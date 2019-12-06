package x.domainpersistencemodeling.child

import io.micronaut.context.event.ApplicationEventPublisher
import lombok.Generated
import x.domainpersistencemodeling.PersistableDomain
import x.domainpersistencemodeling.PersistedDependentDetails
import x.domainpersistencemodeling.PersistedDomain
import x.domainpersistencemodeling.PersistedFactory
import x.domainpersistencemodeling.RecordHolder
import x.domainpersistencemodeling.TrackedManyToOne
import x.domainpersistencemodeling.TrackedOptionalOne
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.other.OtherFactory
import x.domainpersistencemodeling.saveMutated
import x.domainpersistencemodeling.uncurrySecond
import java.time.OffsetDateTime
import java.util.Objects.hash
import javax.inject.Singleton

private typealias UnassignedChildPersistedDomain = PersistedDomain<
        ChildSnapshot,
        ChildRecord,
        PersistedChildDependentDetails,
        PersistedChildFactory,
        UnassignedChild,
        MutableChild>

private typealias AssignedChildPersistedDomain = PersistedDomain<
        ChildSnapshot,
        ChildRecord,
        PersistedChildDependentDetails,
        PersistedChildFactory,
        AssignedChild,
        MutableChild>

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
            record.defaultSideValues,
            record.sideValues,
            record.version
        )

    private fun toDomain(record: ChildRecord): Child<*> {
        val holder = RecordHolder(record)
        val dependent = PersistedChildDependentDetails(
            others.findAssignedTo(record.naturalId),
            holder
        )

        return if (record.assigned) PersistedAssignedChild(
            PersistedDomain(
                this,
                toSnapshot(record, dependent),
                holder,
                dependent,
                ::PersistedAssignedChild
            )
        )
        else PersistedUnassignedChild(
            PersistedDomain(
                this,
                toSnapshot(record, dependent),
                holder,
                dependent,
                ::PersistedUnassignedChild
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

    private val _other = TrackedOptionalOne(
        initialOther,
        { other, _ -> updateRecord(other) },
        { _, _ -> updateRecord(null) })
    override var other: Other? by _other

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun equals(other: Any?) = this === other
            || other is PersistedChildDependentDetails
            && _other == other._other

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun hashCode() = hash(_other)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun toString() = "${super.toString()}{_other=$_other}"

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
    override val defaultSideValues: Set<String> // Sorted
        get() = persisted.record.defaultSideValues
    override val sideValues: Set<String> // Sorted
        get() = persisted.record.sideValues
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

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun equals(other: Any?) = this === other
            || other is PersistedChild<*>
            && persisted == other.persisted

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun hashCode() = hash(persisted)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun toString() = "${super.toString()}{persisted=$persisted}"
}

internal class PersistedUnassignedChild(
    persisted: UnassignedChildPersistedDomain
) : PersistedChild<UnassignedChild>(persisted),
    UnassignedChild {
    init {
        // TODO: require(!assigned)
    }

    /**
     * Assigns this child to a parent, a mutable and _internal_
     * operation called from the parent implementation.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun assignTo(parentNaturalId: String) = let {
        update {
            this.parentNaturalId = parentNaturalId
        }

        PersistedAssignedChild(persisted as AssignedChildPersistedDomain)
    }
}

internal class PersistedAssignedChild(
    persisted: AssignedChildPersistedDomain
) : PersistedChild<AssignedChild>(persisted),
    AssignedChild {
    init {
        // TODO: require(assigned)
    }

    /**
     * Unassigns this child from any parent, a mutable and _internal_
     * operation called from the parent implementation.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun unassignFromAnyParent() = let {
        update {
            parentNaturalId = null
        }

        PersistedUnassignedChild(persisted as UnassignedChildPersistedDomain)
    }
}

internal class PersistedMutableChild(
    private val record: ChildRecord,
    private val persistence: PersistedChildDependentDetails
) : MutableChild,
    MutableChildSimpleDetails by record,
    MutableChildDependentDetails by persistence {

    override val defaultSideValues: MutableSet<String> =
        TrackedManyToOne(
            record.defaultSideValues,
            ::replaceDefaultSideValues.uncurrySecond(),
            ::replaceDefaultSideValues.uncurrySecond()
        )

    override val sideValues: MutableSet<String> =
        TrackedManyToOne(
            record.sideValues,
            ::replaceSideValues.uncurrySecond(),
            ::replaceSideValues.uncurrySecond()
        )

    private fun replaceDefaultSideValues(all: MutableSet<String>) {
        record.defaultSideValues = all
    }

    private fun replaceSideValues(all: MutableSet<String>) {
        record.sideValues = all
    }
}
