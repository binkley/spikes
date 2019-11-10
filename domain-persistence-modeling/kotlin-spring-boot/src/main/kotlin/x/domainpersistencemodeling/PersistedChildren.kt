package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import x.domainpersistencemodeling.ChildRepository.ChildRecord
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.time.OffsetDateTime
import java.util.Objects
import java.util.TreeSet

@Component
internal class PersistedChildFactory(
        private val repository: ChildRepository,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory {
    override fun all(): Sequence<Child<*>> =
            repository.findAll().map {
                toChild(it)
            }.asSequence()

    override fun findExisting(naturalId: String): Child<*>? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                toChild(it)
            }

    override fun createNewUnassigned(naturalId: String): UnassignedChild =
            PersistedUnassignedChild(this, null, ChildRecord(naturalId),
                    PersistedChildComputedDetails())

    override fun findExistingOrCreateNewUnassigned(naturalId: String) =
            findExisting(naturalId) ?: createNewUnassigned(naturalId)

    override fun findAssignedFor(
            parentNaturalId: String): Sequence<AssignedChild> =
            repository.findByParentNaturalId(parentNaturalId).map {
                val computed = PersistedChildComputedDetails()
                PersistedAssignedChild(this, it.toSnapshot(computed), it,
                        computed)
            }.asSequence()

    internal fun save(record: ChildRecord) =
            UpsertedRecordResult(record, repository.upsert(record))

    internal fun delete(record: ChildRecord) =
            repository.delete(record)

    internal fun refreshRecord(naturalId: String) =
            repository.findByNaturalId(naturalId).orElseThrow()

    internal fun notifyChanged(
            before: ChildSnapshot?, after: ChildSnapshot?) =
            publisher.publishEvent(ChildChangedEvent(before, after))

    private fun toChild(record: ChildRecord): PersistedChild<out Child<*>> {
        val computed = PersistedChildComputedDetails()
        return if (null == record.parentNaturalId) PersistedUnassignedChild(
                this, record.toSnapshot(computed), record, computed)
        else PersistedAssignedChild(
                this, record.toSnapshot(computed), record, computed)
    }
}

internal class PersistedChildComputedDetails : ChildComputedDetails {
    internal fun saveMutated() = false
}

/**
 * Notice that this is both an [UnassignedChild], and an [AssignedChild] for
 * ease of implementation.  Externally, this details is not visible, as
 * [PersistedChildFactory] returns only interfaces, not concrete classes, in
 * its factory methods, and downcasting to narrow is safe.
 */
internal open class PersistedChild<C : Child<C>>(
        protected val factory: PersistedChildFactory,
        protected var snapshot: ChildSnapshot?,
        protected var record: ChildRecord?,
        protected val computed: PersistedChildComputedDetails)
    : Child<C> {
    override val naturalId: String
        get() = record().naturalId
    override val otherNaturalId: String?
        get() = record().otherNaturalId
    override val parentNaturalId: String?
        get() = record().parentNaturalId
    override val state: String
        get() = record().state
    override val at: OffsetDateTime
        get() = record().at
    override val value: String?
        get() = record().value
    override val sideValues: Set<String> // Sorted
        get() = TreeSet(record().sideValues)
    override val defaultSideValues: Set<String> // Sorted
        get() = TreeSet(record().defaultSideValues)
    override val version: Int
        get() = record().version
    override val changed
        get() = snapshot != toSnapshot()

    override fun assign(other: Other) = update {
        otherNaturalId = other.naturalId
    }

    override fun unassignAnyOther() = update {
        otherNaturalId = null
    }

    @Suppress("UNCHECKED_CAST")
    @Transactional
    override fun save(): UpsertedDomainResult<ChildSnapshot, C> {
        // Save ourselves first, so children have a valid parent
        val before = snapshot
        var result =
                if (changed) factory.save(record())
                else UpsertedRecordResult(record(), false)
        record = result.record

        if (computed.saveMutated()) {
            // Refresh the version
            record = factory.refreshRecord(naturalId)
            result = UpsertedRecordResult(record(), true)
        }

        val after = record().toSnapshot(computed)
        snapshot = after
        if (result.changed) // Trust the database
            factory.notifyChanged(before, after)
        return UpsertedDomainResult(this as C, result.changed)
    }

    @Transactional
    override fun delete() {
        if (null != parentNaturalId) throw DomainException(
                "Deleting child assigned to a parent: $this")

        val before = snapshot
        computed.saveMutated()
        factory.delete(record())

        val after = null as ChildSnapshot?
        record = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun <R> update(block: MutableChild.() -> R): R =
            PersistedMutableChild(record()).let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedChild<*>
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"

    private fun record() =
            record ?: throw DomainException("Deleted: $this")

    private fun toSnapshot() = record().toSnapshot(computed)
}

internal open class PersistedUnassignedChild(
        factory: PersistedChildFactory,
        snapshot: ChildSnapshot?,
        record: ChildRecord?,
        computed: PersistedChildComputedDetails)
    : PersistedChild<UnassignedChild>(factory, snapshot, record, computed),
        UnassignedChild {
    @Transactional
    override fun assignTo(parent: Parent): AssignedChild = let {
        update {
            parentNaturalId = parent.naturalId
        }
        // In "C"/C++, we could simply cast, since memory layout is identical
        PersistedAssignedChild(factory, snapshot, record, computed)
    }
}

internal open class PersistedAssignedChild(
        factory: PersistedChildFactory,
        snapshot: ChildSnapshot?,
        record: ChildRecord?,
        computed: PersistedChildComputedDetails)
    : PersistedChild<AssignedChild>(factory, snapshot, record, computed),
        AssignedChild {

    @Transactional
    override fun unassignFromAny(): UnassignedChild = let {
        update {
            parentNaturalId = null
        }
        // In "C"/C++, we could simply cast, since memory layout is identical
        PersistedUnassignedChild(factory, snapshot, record, computed)
    }
}

internal class PersistedMutableChild(private val record: ChildRecord)
    : MutableChild,
        MutableChildSimpleDetails by record {
    override val sideValues: MutableSet<String>
        get() = TrackedSortedSet(record.sideValues,
                ::replaceSideValues.uncurrySecond(),
                ::replaceSideValues.uncurrySecond())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableChild
        return record == other.record
    }

    override fun hashCode() = Objects.hash(record)

    override fun toString() =
            "${super.toString()}{record=$record}"

    private fun replaceSideValues(all: MutableSet<String>) {
        record.sideValues = all
    }
}
