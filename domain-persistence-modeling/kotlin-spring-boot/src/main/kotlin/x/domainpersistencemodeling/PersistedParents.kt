package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects
import java.util.Optional
import java.util.TreeSet
import java.util.stream.Collectors.toCollection

internal fun ParentRecord.toSnapshot() = ParentSnapshot(
        naturalId, state, null, value, sideValues, version)

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
                    emptySequence())

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(naturalId)

    fun save(record: ParentRecord) =
            UpsertedRecordResult.of(record, repository.upsert(record))

    internal fun delete(record: ParentRecord) {
        repository.delete(record)
    }

    internal fun refresh(naturalId: String) =
            repository.findByNaturalId(naturalId).orElseThrow()

    internal fun notifyChanged(
            before: ParentSnapshot?, after: ParentSnapshot?) =
            publisher.publishEvent(ParentChangedEvent(before, after))

    private fun toParent(record: ParentRecord) =
            PersistedParent(this, record.toSnapshot(), record,
                    children.findAssignedFor(record.naturalId))
}

internal open class PersistedParent(
        private val factory: PersistedParentFactory,
        private var snapshot: ParentSnapshot?,
        private var record: ParentRecord?,
        assigned: Sequence<AssignedChild>)
    : Parent {
    override val naturalId: String
        get() = record().naturalId
    override val state: String
        get() = record().state
    override val value: String?
        get() = record().value
    override val sideValues: Set<String> // Sorted
        get() = TreeSet(record().sideValues)
    override val version: Int
        get() = record().version
    private var snapshotChildren: Set<AssignedChild>
    private var _children: MutableSet<AssignedChild>

    override val children: Set<AssignedChild>
        get() = _children

    init {
        snapshotChildren = assigned.toSortedSet()
        _children = TreeSet(snapshotChildren)
    }

    @Transactional
    override fun assign(child: UnassignedChild) = let {
        update {
            children += child as AssignedChild
        }
        child as AssignedChild
    }

    @Transactional
    override fun unassign(child: AssignedChild) = let {
        update {
            children -= child
        }
        child as UnassignedChild
    }

    override val changed
        get() = snapshot != record().toSnapshot()

    override fun save(): UpsertedDomainResult<ParentSnapshot, Parent> {
        // Save ourselves first, so children have a valid parent
        val before = snapshot
        var result =
                if (changed) factory.save(record())
                else UpsertedRecordResult.of(record(), Optional.empty())
        record = result.record

        if (saveMutatedChildren()) {
            snapshotChildren = TreeSet(children)
            val refreshed = factory.refresh(naturalId)
            record!!.version = refreshed.version
            result = UpsertedRecordResult.of(record(), Optional.of(refreshed))
        }

        val after = record().toSnapshot()
        snapshot = after
        if (result.changed) // Trust the database
            factory.notifyChanged(before, after)
        return UpsertedDomainResult(this, result.changed)
    }

    override fun delete() {
        if (children.isNotEmpty()) throw DomainException(
                "Deleting parent with assigned children: $this")
        // Removed from current object, but potentially not persisted
        snapshotChildren.forEach { it.save() }

        val before = snapshot
        val after = null as ParentSnapshot?
        factory.delete(record())
        record = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun <R> update(block: MutableParent.() -> R): R =
            PersistedMutableParent(
                    record(), children, ::addChild, ::removeChild).let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParent
        return snapshot == other.snapshot
                && snapshotChildren == other.snapshotChildren
                && record == other.record
                && children == other.children
    }

    override fun hashCode() =
            Objects.hash(snapshot, snapshotChildren, record, children)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, snapshotChildren=$snapshotChildren, record=$record, children=$children}"

    private fun addChild(child: AssignedChild,
            all: MutableSet<AssignedChild>) {
        child.update {
            assignTo(this@PersistedParent)
        }
        _children = all
    }

    private fun removeChild(child: AssignedChild,
            all: MutableSet<AssignedChild>) {
        child.update {
            unassignFromAny()
        }
        _children = all
    }

    private fun assignedChildren(): Set<Child> {
        val assigned = TreeSet(children)
        assigned.removeAll(snapshotChildren)
        return assigned
    }

    private fun unassignedChildren(): Set<Child> {
        val unassigned = TreeSet(snapshotChildren)
        unassigned.removeAll(children)
        return unassigned
    }

    private fun changedChildren(): Set<Child> {
        val changed = TreeSet(snapshotChildren)
        changed.retainAll(children)
        return changed.stream()
                .filter { it.changed }
                .collect(toCollection(::TreeSet))
    }

    private fun saveMutatedChildren(): Boolean {
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
        return mutated
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
        MutableParentDetails by record {
    override val sideValues: MutableSet<String>
        get() = TrackedSortedSet(record.sideValues,
                { _, all -> record.sideValues = all },
                { _, all -> record.sideValues = all })
    override val children = TrackedSortedSet(initial, added, removed)
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
        FROM upsert_parent(:naturalId, :state, :value, :sideValues, :version)
        """)
    fun upsert(
            @Param("naturalId") naturalId: String,
            @Param("state") state: String,
            @Param("value") value: String?,
            @Param("sideValues") sideValues: String,
            @Param("version") version: Int)
            : Optional<ParentRecord>

    @JvmDefault
    fun upsert(entity: ParentRecord): Optional<ParentRecord> {
        val upserted = upsert(
                entity.naturalId,
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
        override var state: String,
        override var value: String?,
        override var sideValues: MutableSet<String>,
        override var version: Int)
    : MutableParentDetails,
        UpsertableRecord<ParentRecord> {
    internal constructor(naturalId: String)
            : this(null, naturalId, ENABLED.name, null, mutableSetOf(), 0)

    @Transient
    override val at = null // TODO: Smell

    override fun upsertedWith(upserted: ParentRecord): ParentRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        state = upserted.state
        value = upserted.value
        sideValues = TreeSet(upserted.sideValues)
        version = upserted.version
        return this
    }
}
