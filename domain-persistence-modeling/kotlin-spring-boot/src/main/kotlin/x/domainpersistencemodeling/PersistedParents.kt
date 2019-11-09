package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.time.OffsetDateTime
import java.util.Objects
import java.util.Optional
import java.util.TreeSet
import java.util.stream.Collectors.toCollection

internal fun ParentRecord.toSnapshot(computed: ParentComputedDetails) =
        ParentSnapshot(naturalId, otherNaturalId, state, computed.at, value,
                sideValues, version)

@Component
internal class PersistedParentFactory(
        private val repository: ParentRepository,
        private val children: ChildFactory,
        private val publisher: ApplicationEventPublisher)
    : ParentFactory {
    override fun all() = repository.findAll().map {
        toParent(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Parent? {
        return repository.findByNaturalId(naturalId).map {
            toParent(it)
        }.orElse(null)
    }

    override fun createNew(naturalId: String) =
            PersistedParent(this, null, ParentRecord(naturalId),
                    PersistedParentComputedDetails(emptySequence()))

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(naturalId)

    fun save(record: ParentRecord) =
            UpsertedRecordResult(record, repository.upsert(record))

    internal fun delete(record: ParentRecord) {
        repository.delete(record)
    }

    internal fun refreshRecord(naturalId: String) =
            repository.findByNaturalId(naturalId).orElseThrow()

    internal fun notifyChanged(
            before: ParentSnapshot?, after: ParentSnapshot?) =
            publisher.publishEvent(ParentChangedEvent(before, after))

    private fun toParent(record: ParentRecord): PersistedParent {
        val computed = PersistedParentComputedDetails(
                children.findAssignedFor(record.naturalId))
        return PersistedParent(this, record.toSnapshot(computed), record,
                computed)
    }
}

internal class PersistedParentComputedDetails(
        assigned: Sequence<AssignedChild>)
    : ParentComputedDetails {
    override val at: OffsetDateTime?
        get() = children.at

    private var snapshotChildren: Set<AssignedChild>
    private var currentChildren: MutableSet<AssignedChild>

    override val children: Set<AssignedChild>
        get() = currentChildren

    init {
        snapshotChildren = assigned.toSortedSet()
        currentChildren = TreeSet(snapshotChildren)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParentComputedDetails
        return snapshotChildren == other.snapshotChildren
                && currentChildren == other.currentChildren
    }

    override fun hashCode() =
            Objects.hash(snapshotChildren, currentChildren)

    override fun toString() =
            "${super.toString()}{snapshotChildren=$snapshotChildren, currentChildren=$currentChildren}"

    internal fun addChild(child: AssignedChild) {
        currentChildren.add(child)
    }

    internal fun removeChild(child: AssignedChild) {
        currentChildren.remove(child)
    }

    internal fun saveMutatedChildren(): Boolean {
        // TODO: Gross function
        var mutated = false
        val assignedChildren = assignedChildren()
        if (assignedChildren.isNotEmpty()) {
            assignedChildren.forEach { it.save() }
            mutated = true
        }
        val unassignedChildren = unassignedChildren()
        if (unassignedChildren.isNotEmpty()) {
            unassignedChildren.forEach { it.save() }
            mutated = true
        }
        val changedChildren = changedChildren()
        if (changedChildren.isNotEmpty()) {
            changedChildren.forEach { it.save() }
            mutated = true
        }

        if (mutated) snapshotChildren = TreeSet(children)

        return mutated
    }

    private fun assignedChildren(): Set<AssignedChild> {
        val assigned = TreeSet(children)
        assigned.removeAll(snapshotChildren)
        return assigned
    }

    private fun unassignedChildren(): Set<AssignedChild> {
        val unassigned = TreeSet(snapshotChildren)
        unassigned.removeAll(children)
        return unassigned
    }

    private fun changedChildren(): Set<AssignedChild> {
        val changed = TreeSet(snapshotChildren)
        changed.retainAll(children)
        return changed.stream()
                .filter { it.changed }
                .collect(toCollection(::TreeSet))
    }
}

internal open class PersistedParent(
        private val factory: PersistedParentFactory,
        private var snapshot: ParentSnapshot?,
        private var record: ParentRecord?,
        private val computed: PersistedParentComputedDetails)
    : Parent {
    override val naturalId: String
        get() = record().naturalId
    override val otherNaturalId: String?
        get() = record().otherNaturalId
    override val state: String
        get() = record().state
    override val at: OffsetDateTime?
        get() = computed.at
    override val value: String?
        get() = record().value
    override val sideValues: Set<String> // Sorted
        get() = TreeSet(record().sideValues)
    override val version: Int
        get() = record().version
    override val children: Set<AssignedChild>
        get() = computed.children
    override val changed
        get() = snapshot != record().toSnapshot(computed)

    override fun assign(other: Other) = update {
        otherNaturalId = other.naturalId
    }

    override fun unassignAnyOther() = update {
        otherNaturalId = null
    }

    override fun assign(child: UnassignedChild) = let {
        val assigned = child.assignTo(this)
        update {
            children += assigned
        }
        assigned
    }

    override fun unassign(child: AssignedChild) = let {
        update {
            children -= child
        }
        child.unassignFromAny()
    }

    /**
     * Notice that when **saving**, save the parent _first_, so added
     * children have a valid FK reference.
     */
    @Transactional
    override fun save(): UpsertedDomainResult<ParentSnapshot, Parent> {
        // Save ourselves first, so children have a valid parent
        val before = snapshot
        var result =
                if (changed) factory.save(record())
                else UpsertedRecordResult(record(), false)
        record = result.record

        if (computed.saveMutatedChildren()) {
            // Refresh the version
            record = factory.refreshRecord(naturalId)
            result = UpsertedRecordResult(record(), true)
        }

        val after = record().toSnapshot(computed)
        snapshot = after
        if (result.changed) // Trust the database
            factory.notifyChanged(before, after)
        return UpsertedDomainResult(this, result.changed)
    }

    /**
     * Notice that when **deleting**, save the parent _last_, so that FK
     * references get cleared.
     */
    @Transactional
    override fun delete() {
        if (children.isNotEmpty()) throw DomainException(
                "Deleting parent with assigned children: $this")

        val before = snapshot
        computed.saveMutatedChildren()
        factory.delete(record())

        val after = null as ParentSnapshot?
        record = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun <R> update(block: MutableParent.() -> R): R =
            PersistedMutableParent(record(), children,
                    ::addChild.uncurryFirst(),
                    ::removeChild.uncurryFirst())
                    .let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParent
        return snapshot == other.snapshot
                && record == other.record
                && computed == other.computed
    }

    override fun hashCode() =
            Objects.hash(snapshot, record, computed)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record, computed=$computed}"

    private fun addChild(child: AssignedChild) {
        computed.addChild(child)
    }

    private fun removeChild(child: AssignedChild) {
        computed.removeChild(child)
    }

    private fun record() =
            record ?: throw DomainException("Deleted: $this")
}

internal data class PersistedMutableParent(
        private val record: ParentRecord,
        private val initial: Set<AssignedChild>,
        private val added: (AssignedChild, MutableSet<AssignedChild>) -> Unit,
        private val removed: (AssignedChild, MutableSet<AssignedChild>) -> Unit)
    : MutableParent,
        MutableParentSimpleDetails by record {
    override val sideValues = TrackedSortedSet(record.sideValues,
            ::replaceSideValues.uncurrySecond(),
            ::replaceSideValues.uncurrySecond())
    override val children = TrackedSortedSet(initial, added, removed)

    private fun replaceSideValues(all: MutableSet<String>) {
        record.sideValues = all
    }
}

interface ParentRepository : CrudRepository<ParentRecord, Long> {
    @Query("""
        SELECT *
        FROM parent
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<ParentRecord>

    @Query("""
        SELECT *
        FROM upsert_parent(:naturalId, :otherNaturalId, :state, :value, :sideValues, :version)
        """)
    fun upsert(
            @Param("naturalId") naturalId: String,
            @Param("otherNaturalId") otherNaturalId: String?,
            @Param("state") state: String,
            @Param("value") value: String?,
            @Param("sideValues") sideValues: String,
            @Param("version") version: Int)
            : Optional<ParentRecord>

    @JvmDefault
    fun upsert(entity: ParentRecord): Optional<ParentRecord> {
        val upserted = upsert(
                entity.naturalId,
                entity.otherNaturalId,
                entity.state,
                entity.value,
                entity.sideValues.workAroundArrayTypeForPostgres(),
                entity.version)
        upserted.ifPresent {
            entity.upsertedWith(it)
        }
        return upserted
    }
}

@Table("parent")
data class ParentRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var otherNaturalId: String?,
        override var state: String,
        override var value: String?,
        override var sideValues: MutableSet<String>,
        override var version: Int)
    : MutableParentSimpleDetails,
        UpsertableRecord<ParentRecord> {
    internal constructor(naturalId: String)
            : this(null, naturalId, null, ENABLED.name, null, mutableSetOf(),
            0)

    override fun upsertedWith(upserted: ParentRecord): ParentRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        otherNaturalId = upserted.otherNaturalId
        state = upserted.state
        value = upserted.value
        sideValues = TreeSet(upserted.sideValues)
        version = upserted.version
        return this
    }
}
