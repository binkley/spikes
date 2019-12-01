package x.domainpersistencemodeling.child

import io.micronaut.context.event.ApplicationEventPublisher
import x.domainpersistencemodeling.*
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.other.OtherFactory
import java.time.OffsetDateTime
import java.util.*
import java.util.Objects.hash
import javax.inject.Singleton

@Singleton
internal class PersistedChildFactory(
    private val repository: ChildRepository,
    private val others: OtherFactory,
    private val publisher: ApplicationEventPublisher
) : ChildFactory,
    PersistedFactory<ChildSnapshot, ChildRecord, PersistedChildDependentDetails> {
    override fun all(): Sequence<Child<*>> =
        repository.findAll().map {
            toDomain(it)
        }.asSequence()

    override fun findExisting(naturalId: String): Child<*>? =
        repository.findByNaturalId(naturalId).orElse(null)?.let {
            toDomain(it)
        }

    override fun createNewUnassigned(naturalId: String): UnassignedChild {
        val holder = RecordHolder(ChildRecord(naturalId))
        return PersistedUnassignedChild(
            PersistedDomain(
                this,
                null,
                holder,
                PersistedChildDependentDetails(null, holder),
                ::PersistedUnassignedChild
            )
        )
    }

    override fun findExistingOrCreateNewUnassigned(naturalId: String) =
        findExisting(naturalId) ?: createNewUnassigned(naturalId)

    override fun findAssignedTo(parentNaturalId: String)
            : Sequence<AssignedChild> =
        repository.findByParentNaturalId(parentNaturalId).map {
            toDomain(it) as AssignedChild
        }.asSequence()

    override fun save(record: ChildRecord) =
        UpsertedRecordResult(record, repository.upsert(record))

    override fun delete(record: ChildRecord) =
        repository.delete(record)

    override fun refreshPersistence(naturalId: String): ChildRecord =
        repository.findByNaturalId(naturalId).orElseThrow()

    override fun notifyChanged(
        before: ChildSnapshot?, after: ChildSnapshot?
    ) =
        publisher.publishEvent(ChildChangedEvent(before, after))

    override fun toSnapshot(
        record: ChildRecord,
        dependent: PersistedChildDependentDetails
    ) =
        ChildSnapshot(
            record.naturalId,
            record.otherNaturalId,
            record.parentNaturalId,
            record.state,
            record.at,
            record.value,
            record.sideValues,
            record.version
        )

    private fun toDomain(record: ChildRecord): Child<*> {
        val holder = RecordHolder(record)
        val dependent = PersistedChildDependentDetails(
            others.findAssignedTo(record.naturalId),
            holder
        )

        return if (null == record.parentNaturalId) PersistedUnassignedChild(
            PersistedDomain(
                this,
                toSnapshot(record, dependent),
                holder,
                dependent,
                ::PersistedUnassignedChild
            )
        ) else PersistedAssignedChild(
            PersistedDomain(
                this,
                toSnapshot(record, dependent),
                holder,
                dependent,
                ::PersistedAssignedChild
            )
        )
    }
}

internal class PersistedChildDependentDetails(
    initialOther: Other?,
    private val holder: RecordHolder<ChildRecord>
) : ChildDependentDetails,
    PersistedDependentDetails<ChildRecord>,
    MutableChildDependentDetails {
    override fun saveMutated() = _other.saveMutated()

    private val _other = TrackedSortedSet(
        if (null == initialOther) emptySet() else setOf(initialOther),
        { other, _ -> updateRecord(other) },
        { _, _ -> updateRecord(null) })
    override var other: Other? by _other

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedChildDependentDetails
        return _other == other._other
    }

    override fun hashCode() = hash(_other)

    override fun toString() =
        "${super.toString()}{_other=$_other}"

    private fun updateRecord(other: Other?) {
        holder.record!!.otherNaturalId = other?.naturalId
    }
}

/**
 * Notice that this is both an [UnassignedChild], and an [AssignedChild] for
 * ease of implementation.  Externally, this details is not visible, as
 * [PersistedChildFactory] returns only interfaces, not concrete classes, in
 * its factory methods, and downcasting to narrow is safe.
 */
internal open class PersistedChild<C : Child<C>>(
    protected val persisted: PersistedDomain<
            ChildSnapshot,
            ChildRecord,
            PersistedChildDependentDetails,
            PersistedChildFactory,
            C,
            MutableChild>
) : Child<C>,
    PersistableDomain<ChildSnapshot, C> by persisted {
    override val parentNaturalId: String?
        get() = persisted.record.parentNaturalId
    override val state: String
        get() = persisted.record.state
    override val at: OffsetDateTime
        get() = persisted.record.at
    override val value: String?
        get() = persisted.record.value
    override val sideValues: Set<String> // Sorted
        get() = TreeSet(persisted.record.sideValues)
    override val defaultSideValues: Set<String> // Sorted
        get() = TreeSet(persisted.record.defaultSideValues)
    override val other: Other?
        get() = persisted.dependent.other

    override fun <R> update(block: MutableChild.() -> R): R =
        PersistedMutableChild(
            persisted.record,
            persisted.dependent
        ).let(block)

    override fun assign(other: Other) = update {
        persisted.dependent.other = other
    }

    override fun unassignAnyOther() = update {
        persisted.dependent.other = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedChild<*>
        return persisted == other.persisted
    }

    override fun hashCode() = persisted.hashCode()

    override fun toString() = "${super.toString()}$persisted"
}

internal class PersistedUnassignedChild(
    persisted: PersistedDomain<
            ChildSnapshot,
            ChildRecord,
            PersistedChildDependentDetails,
            PersistedChildFactory,
            UnassignedChild,
            MutableChild>
) : PersistedChild<UnassignedChild>(persisted),
    UnassignedChild {
    /** Assigns this child to a parent, a mutable operation. */
    @Suppress("UNCHECKED_CAST")
    internal fun assignTo(parentNaturalId: String) = let {
        update {
            this.parentNaturalId = parentNaturalId
        }

        PersistedAssignedChild(
            persisted as PersistedDomain<
                    ChildSnapshot,
                    ChildRecord,
                    PersistedChildDependentDetails,
                    PersistedChildFactory,
                    AssignedChild,
                    MutableChild>
        )
    }
}

internal class PersistedAssignedChild(
    persisted: PersistedDomain<
            ChildSnapshot,
            ChildRecord,
            PersistedChildDependentDetails,
            PersistedChildFactory,
            AssignedChild,
            MutableChild>
) : PersistedChild<AssignedChild>(persisted),
    AssignedChild {
    /** Unassigns this child from any parent, a mutable operation. */
    @Suppress("UNCHECKED_CAST")
    internal fun unassignFromAny() = let {
        update {
            parentNaturalId = null
        }

        PersistedUnassignedChild(
            persisted as PersistedDomain<
                    ChildSnapshot,
                    ChildRecord,
                    PersistedChildDependentDetails,
                    PersistedChildFactory,
                    UnassignedChild,
                    MutableChild>
        )
    }
}

internal class PersistedMutableChild(
    private val record: ChildRecord,
    private val persistence: PersistedChildDependentDetails
) : MutableChild,
    MutableChildSimpleDetails by record,
    MutableChildDependentDetails by persistence {
    override val sideValues: MutableSet<String>
        get() = TrackedSortedSet(
            record.sideValues,
            ::replaceSideValues.uncurrySecond(),
            ::replaceSideValues.uncurrySecond()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableChild
        return record == other.record
    }

    override fun hashCode() = hash(record)

    override fun toString() =
        "${super.toString()}{record=$record}"

    private fun replaceSideValues(all: MutableSet<String>) {
        record.sideValues = all
    }
}
