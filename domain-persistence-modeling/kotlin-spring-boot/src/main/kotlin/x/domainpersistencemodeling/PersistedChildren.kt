package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import x.domainpersistencemodeling.ChildRepository.ChildRecord
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.time.OffsetDateTime
import java.util.Objects
import java.util.TreeSet

@Component
internal class PersistedChildFactory(
        private val repository: ChildRepository,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory,
        PersistedFactory<ChildSnapshot, ChildRecord, PersistedChildComputedDetails> {
    override fun all(): Sequence<Child<*>> =
            repository.findAll().map {
                toDomain(it)
            }.asSequence()

    override fun findExisting(naturalId: String): Child<*>? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                toDomain(it)
            }

    override fun createNewUnassigned(naturalId: String) =
            createNew(null, ChildRecord(naturalId),
                    ::PersistedUnassignedChild)

    override fun findExistingOrCreateNewUnassigned(naturalId: String) =
            findExisting(naturalId) ?: createNewUnassigned(naturalId)

    override fun findAssignedFor(parentNaturalId: String)
            : Sequence<AssignedChild> =
            repository.findByParentNaturalId(parentNaturalId).map {
                createNew(toSnapshot(it, PersistedChildComputedDetails()), it,
                        ::PersistedAssignedChild)
            }.asSequence()

    override fun save(record: ChildRecord) =
            UpsertedRecordResult(record, repository.upsert(record))

    override fun delete(record: ChildRecord) =
            repository.delete(record)

    override fun refreshRecord(naturalId: String): ChildRecord =
            repository.findByNaturalId(naturalId).orElseThrow()

    override fun notifyChanged(
            before: ChildSnapshot?, after: ChildSnapshot?) =
            publisher.publishEvent(ChildChangedEvent(before, after))

    override fun toSnapshot(record: ChildRecord,
            computed: PersistedChildComputedDetails) =
            ChildSnapshot(record.naturalId, record.otherNaturalId,
                    record.parentNaturalId, record.state, record.at,
                    record.value, record.sideValues, record.version)

    private fun toDomain(record: ChildRecord): Child<*> {
        val computed = PersistedChildComputedDetails()

        return if (null == record.parentNaturalId)
            createNew(toSnapshot(record, computed), record,
                    ::PersistedUnassignedChild)
        else
            createNew(toSnapshot(record, computed), record,
                    ::PersistedAssignedChild)
    }

    private fun <C : Child<C>> createNew(
            snapshot: ChildSnapshot?,
            record: ChildRecord,
            toDomain: (PersistedDomain<ChildSnapshot, ChildRecord, PersistedChildComputedDetails, PersistedChildFactory, C, MutableChild>) -> C) =
            toDomain(PersistedDomain(this, snapshot, record,
                    PersistedChildComputedDetails(),
                    toDomain))
}

internal data class PersistedChildComputedDetails(
        private val saveMutated: Boolean = false)
    : ChildComputedDetails,
        PersistedComputedDetails {
    override fun saveMutated() = saveMutated
}

/**
 * Notice that this is both an [UnassignedChild], and an [AssignedChild] for
 * ease of implementation.  Externally, this details is not visible, as
 * [PersistedChildFactory] returns only interfaces, not concrete classes, in
 * its factory methods, and downcasting to narrow is safe.
 */
internal open class PersistedChild<C : Child<C>>(
        protected val persisted: PersistedDomain<ChildSnapshot, ChildRecord, PersistedChildComputedDetails, PersistedChildFactory, C, MutableChild>)
    : Child<C>,
        PersistableDomain<ChildSnapshot, C> by persisted {
    override val otherNaturalId: String?
        get() = persisted.record().otherNaturalId
    override val parentNaturalId: String?
        get() = persisted.record().parentNaturalId
    override val state: String
        get() = persisted.record().state
    override val at: OffsetDateTime
        get() = persisted.record().at
    override val value: String?
        get() = persisted.record().value
    override val sideValues: Set<String> // Sorted
        get() = TreeSet(persisted.record().sideValues)
    override val defaultSideValues: Set<String> // Sorted
        get() = TreeSet(persisted.record().defaultSideValues)

    override fun <R> update(block: MutableChild.() -> R): R =
            PersistedMutableChild(persisted.record()).let(block)

    override fun assign(other: Other) = update {
        otherNaturalId = other.naturalId
    }

    override fun unassignAnyOther() = update {
        otherNaturalId = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedChild<*>
        return persisted.snapshot == other.persisted.snapshot
                && persisted.record == other.persisted.record
                && persisted.computed == other.persisted.computed
    }

    override fun hashCode() =
            Objects.hash(persisted.snapshot, persisted.record,
                    persisted.computed)

    override fun toString() =
            "${super.toString()}{snapshot=${persisted.snapshot}, record=${persisted.record}, computed=${persisted.computed}}"
}

internal open class PersistedUnassignedChild(
        persisted: PersistedDomain<ChildSnapshot, ChildRecord, PersistedChildComputedDetails, PersistedChildFactory, UnassignedChild, MutableChild>)
    : PersistedChild<UnassignedChild>(persisted),
        UnassignedChild {
    /** Assigns this child to a parent, a mutable operation. */
    @Suppress("UNCHECKED_CAST")
    internal fun assignTo(parentNaturalId: String) = let {
        update {
            this.parentNaturalId = parentNaturalId
        }

        PersistedAssignedChild(
                persisted as PersistedDomain<ChildSnapshot, ChildRecord, PersistedChildComputedDetails, PersistedChildFactory, AssignedChild, MutableChild>)
    }
}

internal open class PersistedAssignedChild(
        persisted: PersistedDomain<ChildSnapshot, ChildRecord, PersistedChildComputedDetails, PersistedChildFactory, AssignedChild, MutableChild>)
    : PersistedChild<AssignedChild>(persisted),
        AssignedChild {
    /** Unassigns this child from any parent, a mutable operation. */
    @Suppress("UNCHECKED_CAST")
    internal fun unassignFromAny() = let {
        update {
            parentNaturalId = null
        }

        PersistedUnassignedChild(
                persisted as PersistedDomain<ChildSnapshot, ChildRecord, PersistedChildComputedDetails, PersistedChildFactory, UnassignedChild, MutableChild>)
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
