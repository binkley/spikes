package x.domainpersistencemodeling.parent

import io.micronaut.context.event.ApplicationEventPublisher
import lombok.Generated
import x.domainpersistencemodeling.DomainException
import x.domainpersistencemodeling.PersistableDomain
import x.domainpersistencemodeling.PersistedDependentDetails
import x.domainpersistencemodeling.PersistedDomain
import x.domainpersistencemodeling.PersistedFactory
import x.domainpersistencemodeling.RecordHolder
import x.domainpersistencemodeling.TrackedManyToOne
import x.domainpersistencemodeling.TrackedOptionalOne
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import x.domainpersistencemodeling.child.AssignedChild
import x.domainpersistencemodeling.child.ChildFactory
import x.domainpersistencemodeling.child.PersistedAssignedChild
import x.domainpersistencemodeling.child.PersistedUnassignedChild
import x.domainpersistencemodeling.child.UnassignedChild
import x.domainpersistencemodeling.computeDue
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.other.OtherFactory
import x.domainpersistencemodeling.saveMutated
import x.domainpersistencemodeling.uncurryFirst
import x.domainpersistencemodeling.uncurrySecond
import java.time.OffsetDateTime
import java.util.Objects.hash
import javax.inject.Singleton

@Singleton
internal class PersistedParentFactory(
    private val repository: ParentRepository,
    private val others: OtherFactory,
    private val children: ChildFactory,
    private val publisher: ApplicationEventPublisher
) : ParentFactory,
    PersistedFactory<
            ParentSnapshot,
            ParentRecord,
            PersistedParentDependentDetails> {
    override fun all() = repository.findAll().map {
        toDomain(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Parent? {
        return repository.findByNaturalId(naturalId).map {
            toDomain(it)
        }.orElse(null)
    }

    override fun createNew(naturalId: String): Parent {
        val holder = RecordHolder(ParentRecord(naturalId))
        return PersistedParent(
            PersistedDomain(
                this,
                null,
                holder,
                PersistedParentDependentDetails(null, emptySet(), holder),
                ::PersistedParent
            )
        )
    }

    override fun findExistingOrCreateNew(naturalId: String) =
        findExisting(naturalId) ?: createNew(naturalId)

    override fun save(record: ParentRecord) =
        UpsertedRecordResult(record, repository.upsert(record))

    override fun delete(record: ParentRecord) {
        repository.delete(record)
    }

    override fun refreshPersistence(naturalId: String): ParentRecord =
        repository.findByNaturalId(naturalId).orElseThrow()

    override fun toSnapshot(
        record: ParentRecord,
        dependent: PersistedParentDependentDetails
    ) =
        ParentSnapshot(
            record.naturalId,
            record.otherNaturalId,
            record.state,
            dependent.due,
            record.at,
            record.value,
            record.sideValues,
            record.version
        )

    override fun notifyChanged(
        before: ParentSnapshot?, after: ParentSnapshot?
    ) =
        publisher.publishEvent(ParentChangedEvent(before, after))

    private fun toDomain(record: ParentRecord): PersistedParent {
        val holder = RecordHolder(record)
        val dependent = PersistedParentDependentDetails(
            others.findAssignedTo(record.naturalId),
            children.findAssignedTo(record.naturalId).toSortedSet(),
            holder
        )

        return PersistedParent(
            PersistedDomain(
                this,
                toSnapshot(record, dependent),
                holder,
                dependent,
                ::PersistedParent
            )
        )
    }
}

internal class PersistedParentDependentDetails(
    initialOther: Other?,
    initialChildren: Set<AssignedChild>,
    private val holder: RecordHolder<ParentRecord>
) : ParentDependentDetails,
    PersistedDependentDetails<ParentRecord>,
    MutableParentDependentDetails {
    override fun saveMutated() =
        _other.saveMutated() or _children.saveMutated()

    private val _other = TrackedOptionalOne(
        initialOther,
        ::updateRecord.uncurryFirst(),
        { _, _ -> updateRecord(null) })
    override var other: Other? by _other

    private val _children = TrackedManyToOne(
        initialChildren, { _, _ -> }, { _, _ -> })
    override val children = _children

    override val due: OffsetDateTime?
        get() = computeDue(holder.record!!, _children)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun equals(other: Any?) = this === other
            || other is PersistedParentDependentDetails
            && _other == other._other
            && _children == other._children

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun hashCode() = hash(_other, _children)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun toString() =
        "${super.toString()}{_other=$_other, _children=$_children}"

    private fun updateRecord(other: Other?) {
        holder.record!!.otherNaturalId = other?.naturalId
    }
}

internal open class PersistedParent(
    private val persisted: PersistedDomain<ParentSnapshot,
            ParentRecord,
            PersistedParentDependentDetails,
            PersistedParentFactory,
            Parent,
            MutableParent>
) : Parent,
    PersistableDomain<ParentSnapshot, Parent> by persisted {
    override val state: String
        get() = persisted.record.state
    override val due: OffsetDateTime?
        get() = persisted.dependent.due
    override val at: OffsetDateTime?
        get() = persisted.record.at
    override val value: String?
        get() = persisted.record.value
    override val sideValues: Set<String> // Sorted
        get() = persisted.record.sideValues
    override val other: Other?
        get() = persisted.dependent.other
    override val children: Set<AssignedChild>
        get() = persisted.dependent.children

    override fun assign(other: Other) = update {
        persisted.dependent.other = other
    }

    override fun unassignAnyOther() = update {
        persisted.dependent.other = null
    }

    override fun assign(child: UnassignedChild) = let {
        // This wart works around Java/Kotlin having no sense of `friend` as
        // in "C"/C++
        child as PersistedUnassignedChild

        val assigned = child.assignTo(naturalId)
        update {
            children += assigned
        }
        assigned
    }

    override fun unassign(child: AssignedChild) = let {
        // This wart works around Java/Kotlin having no sense of `friend` as
        // in "C"/C++
        child as PersistedAssignedChild

        update {
            children -= child
        }
        child.unassignFromAnyParent()
    }

    override fun delete() {
        if (children.isNotEmpty()) throw DomainException(
            "Deleting parent with assigned children: $this"
        )

        persisted.delete()
    }

    override fun <R> update(block: MutableParent.() -> R): R =
        PersistedMutableParent(
            persisted.record,
            persisted.dependent
        ).let(block)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun equals(other: Any?) = this === other
            || other is PersistedParent
            && persisted == other.persisted

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun hashCode() = hash(persisted)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun toString() = "${super.toString()}{persisted=$persisted}"
}

internal data class PersistedMutableParent(
    private val record: ParentRecord,
    private val persistence: PersistedParentDependentDetails
) : MutableParent,
    MutableParentSimpleDetails by record,
    MutableParentDependentDetails by persistence {
    override val sideValues: MutableSet<String> =
        TrackedManyToOne(
            record.sideValues,
            ::replaceSideValues.uncurrySecond(),
            ::replaceSideValues.uncurrySecond()
        )

    private fun replaceSideValues(all: MutableSet<String>) {
        record.sideValues = all
    }
}
