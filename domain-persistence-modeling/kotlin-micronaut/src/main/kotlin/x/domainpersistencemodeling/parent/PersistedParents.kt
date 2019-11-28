package x.domainpersistencemodeling.parent

import io.micronaut.context.event.ApplicationEventPublisher
import x.domainpersistencemodeling.*
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import x.domainpersistencemodeling.child.*
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.other.OtherFactory
import java.time.OffsetDateTime
import java.util.*
import java.util.Objects.hash
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
    initialOther: Other?,
    initialChildren: Set<AssignedChild>
) : ParentDependentDetails,
    PersistedDependentDetails {
    override fun saveMutated() = sequenceOf(
        _other.saveMutated(),
        _children.saveMutated()
    ).fold(false) { a, b ->
        a || b
    }

    override val at: OffsetDateTime?
        get() = _children.at

    private val _other = TrackedSortedSet(
        if (null == initialOther) emptySet() else setOf(initialOther),
        { _, _ -> }, { _, _ -> })
    override val other: Other?
        get() = _other.firstOrNull()

    private val _children = TrackedSortedSet(
        initialChildren,
        { _, _ -> }, { _, _ -> }
    )
    override val children: Set<AssignedChild>
        get() = _children

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParentDependentDetails
        return _other == other._other
                && _children == other._children
    }

    override fun hashCode() = hash(_other, _children)

    override fun toString() =
        "${super.toString()}{_other=$_other, _children=$_children}"

    internal fun setOther(other: Other?) {
        _other.clear()
        other?.run { _other.add(other) }
    }

    internal fun addChild(child: AssignedChild) {
        _children.add(child)
    }

    internal fun removeChild(child: AssignedChild) {
        _children.remove(child)
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
