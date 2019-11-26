package x.domainpersistencemodeling.parent

import io.micronaut.context.event.ApplicationEventPublisher
import x.domainpersistencemodeling.DomainException
import x.domainpersistencemodeling.PersistableDomain
import x.domainpersistencemodeling.PersistedDependentDetails
import x.domainpersistencemodeling.PersistedDomain
import x.domainpersistencemodeling.PersistedFactory
import x.domainpersistencemodeling.TrackedSortedSet
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import x.domainpersistencemodeling.at
import x.domainpersistencemodeling.child.AssignedChild
import x.domainpersistencemodeling.child.ChildFactory
import x.domainpersistencemodeling.child.PersistedAssignedChild
import x.domainpersistencemodeling.child.PersistedUnassignedChild
import x.domainpersistencemodeling.child.UnassignedChild
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.other.OtherFactory
import x.domainpersistencemodeling.uncurryFirst
import x.domainpersistencemodeling.uncurrySecond
import java.time.OffsetDateTime
import java.util.Objects.hash
import java.util.TreeSet
import javax.inject.Singleton

@Singleton
internal class PersistedParentFactory(
    private val repository: ParentRepository,
    private val others: OtherFactory,
    private val children: ChildFactory,
    private val publisher: ApplicationEventPublisher
) : ParentFactory,
    PersistedFactory<ParentSnapshot, ParentRecord, PersistedParentDependentDetails> {
    override fun all() = repository.findAll().map {
        toDomain(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Parent? {
        return repository.findByNaturalId(naturalId).map {
            toDomain(it)
        }.orElse(null)
    }

    override fun createNew(naturalId: String) =
        PersistedParent(
            PersistedDomain(
                this,
                null,
                ParentRecord(naturalId),
                PersistedParentDependentDetails(null, emptySet()),
                ::PersistedParent
            )
        )

    override fun findExistingOrCreateNew(naturalId: String) =
        findExisting(naturalId) ?: createNew(naturalId)

    override fun save(record: ParentRecord) =
        UpsertedRecordResult(record, repository.upsert(record))

    override fun delete(record: ParentRecord) {
        repository.delete(record)
    }

    override fun refreshRecord(naturalId: String) =
        repository.findByNaturalId(naturalId).orElseThrow()

    override fun toSnapshot(
        record: ParentRecord,
        dependent: PersistedParentDependentDetails
    ) =
        ParentSnapshot(
            record.naturalId,
            record.otherNaturalId,
            record.state,
            dependent.at,
            record.value,
            record.sideValues,
            record.version
        )

    override fun notifyChanged(
        before: ParentSnapshot?, after: ParentSnapshot?
    ) =
        publisher.publishEvent(ParentChangedEvent(before, after))

    private fun toDomain(record: ParentRecord): PersistedParent {
        val dependent = PersistedParentDependentDetails(
            others.findAssignedTo(record.naturalId),
            children.findAssignedTo(record.naturalId).toSortedSet()
        )

        return PersistedParent(
            PersistedDomain(
                this,
                toSnapshot(record, dependent),
                record,
                dependent,
                ::PersistedParent
            )
        )
    }
}

internal class PersistedParentDependentDetails(
    private val initialOther: Other?,
    initialChildren: Set<AssignedChild>
) : ParentDependentDetails,
    PersistedDependentDetails {
    override fun saveMutated() = saveMutatedChildren()

    override val at: OffsetDateTime?
        get() = _children.at

    private var currentOther: Other? = initialOther
    override val other: Other?
        get() = currentOther

    private val _children = TrackedSortedSet(
        initialChildren,
        { one, all -> }, { one, all -> }
    )
    override val children: Set<AssignedChild>
        get() = _children

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParentDependentDetails
        return initialOther == other.initialOther
                && currentOther == other.currentOther
                && _children == other._children
    }

    override fun hashCode() =
        hash(initialOther, currentOther, _children)

    override fun toString() =
        "${super.toString()}{initialOther=$initialOther, currentOther=$currentOther, _children=$_children}"

    internal fun setOther(other: Other?) {
        currentOther = other
    }

    internal fun addChild(child: AssignedChild) {
        _children.add(child)
    }

    internal fun removeChild(child: AssignedChild) {
        _children.remove(child)
    }

    private fun saveMutatedChildren(): Boolean {
        // TODO: Gross function
        var mutated = false
        val assignedChildren = _children.added()
        if (assignedChildren.isNotEmpty()) {
            assignedChildren.forEach { it.save() }
            mutated = true
        }
        val unassignedChildren = _children.removed()
        if (unassignedChildren.isNotEmpty()) {
            unassignedChildren.forEach { it.save() }
            mutated = true
        }
        val changedChildren = _children.changed { it.changed }
        if (changedChildren.isNotEmpty()) {
            changedChildren.forEach { it.save() }
            mutated = true
        }

        if (mutated) _children.reset()

        return mutated
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
    override val otherNaturalId: String?
        get() = persisted.record.otherNaturalId
    override val state: String
        get() = persisted.record.state
    override val at: OffsetDateTime?
        get() = persisted.dependent.at
    override val value: String?
        get() = persisted.record.value
    override val sideValues: Set<String> // Sorted
        get() = TreeSet(persisted.record.sideValues)
    override val other: Other?
        get() = persisted.dependent.other
    override val children: Set<AssignedChild>
        get() = persisted.dependent.children

    override fun assign(other: Other) = update {
        otherNaturalId = other.naturalId
    }

    override fun unassignAnyOther() = update {
        otherNaturalId = null
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
        child.unassignFromAny()
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
            persisted.dependent.other,
            ::setOther,
            persisted.dependent.children,
            ::addChild.uncurryFirst(),
            ::removeChild.uncurryFirst()
        ).let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParent
        return persisted == other.persisted
    }

    override fun hashCode() = persisted.hashCode()

    override fun toString() = "${super.toString()}$persisted"

    private fun setOther(other: Other?) {
        persisted.dependent.setOther(other)
    }

    private fun addChild(child: AssignedChild) {
        persisted.dependent.addChild(child)
    }

    private fun removeChild(child: AssignedChild) {
        persisted.dependent.removeChild(child)
    }
}

internal data class PersistedMutableParent(
    private val record: ParentRecord,
    private var currentOther: Other?,
    private val setOther: (Other?) -> Unit,
    private val initialChildren: Set<AssignedChild>,
    private val addChild:
        (AssignedChild, MutableSet<AssignedChild>) -> Unit,
    private val removeChild:
        (AssignedChild, MutableSet<AssignedChild>) -> Unit
) : MutableParent,
    MutableParentSimpleDetails by record {
    override val at: OffsetDateTime?
        get() = children.at
    override val sideValues =
        TrackedSortedSet(
            record.sideValues,
            ::replaceSideValues.uncurrySecond(),
            ::replaceSideValues.uncurrySecond()
        )
    override var other: Other?
        get() = currentOther
        set(value) {
            currentOther = value
            setOther(value)
        }
    override val children =
        TrackedSortedSet(initialChildren, addChild, removeChild)

    private fun replaceSideValues(all: MutableSet<String>) {
        record.sideValues = all
    }
}
