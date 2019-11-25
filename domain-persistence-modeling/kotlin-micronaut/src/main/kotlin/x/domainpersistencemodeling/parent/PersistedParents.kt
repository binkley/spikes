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
import x.domainpersistencemodeling.uncurryFirst
import x.domainpersistencemodeling.uncurrySecond
import x.domainpersistencemodeling.workAroundArrayTypeForPostgresRead
import java.time.OffsetDateTime
import java.util.*
import java.util.stream.Collectors.toCollection
import javax.inject.Singleton

@Singleton
internal class PersistedParentFactory(
        private val repository: ParentRepository,
        private val children: ChildFactory,
        private val publisher: ApplicationEventPublisher)
    : ParentFactory,
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
            PersistedParent(PersistedDomain(
                    this,
                    null,
                    ParentRecord(naturalId),
                    PersistedParentDependentDetails(emptySequence()),
                    ::PersistedParent))

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(naturalId)

    override fun save(record: ParentRecord) =
            UpsertedRecordResult(record, repository.upsert(record))

    override fun delete(record: ParentRecord) {
        repository.delete(record)
    }

    override fun refreshRecord(naturalId: String): ParentRecord {
        val record = repository.findByNaturalId(naturalId).orElseThrow()

        fix(record)
        
        return record
    }

    override fun toSnapshot(record: ParentRecord,
            dependent: PersistedParentDependentDetails) =
            ParentSnapshot(
                    record.naturalId, record.otherNaturalId,
                    record.state, dependent.at, record.value,
                    record.sideValues, record.version)

    override fun notifyChanged(
            before: ParentSnapshot?, after: ParentSnapshot?) =
            publisher.publishEvent(ParentChangedEvent(before, after))

    private fun toDomain(record: ParentRecord): PersistedParent {
        val dependent = PersistedParentDependentDetails(
                children.findAssignedFor(record.naturalId))

        fix(record)

        return PersistedParent(PersistedDomain(
                this,
                toSnapshot(record, dependent),
                record,
                dependent,
                ::PersistedParent))
    }

    private fun fix(record: ParentRecord) {
        record.sideValues = record.sideValues.workAroundArrayTypeForPostgresRead()
    }
}

internal class PersistedParentDependentDetails(
        initialChildren: Sequence<AssignedChild>)
    : ParentDependentDetails,
        PersistedDependentDetails {
    override fun saveMutated() = saveMutatedChildren()

    override val at: OffsetDateTime?
        get() = children.at

    private var initialChildren: Set<AssignedChild> =
            initialChildren.toSortedSet()
    private var currentChildren: MutableSet<AssignedChild> =
            TreeSet(this.initialChildren)

    override val children: Set<AssignedChild>
        get() = currentChildren

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParentDependentDetails
        return initialChildren == other.initialChildren
                && currentChildren == other.currentChildren
    }

    override fun hashCode() =
            Objects.hash(initialChildren, currentChildren)

    override fun toString() =
            "${super.toString()}{snapshotChildren=$initialChildren, currentChildren=$currentChildren}"

    internal fun addChild(child: AssignedChild) {
        currentChildren.add(child)
    }

    internal fun removeChild(child: AssignedChild) {
        currentChildren.remove(child)
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

        if (mutated) initialChildren = TreeSet(children)

        return mutated
    }

    private fun assignedChildren(): Set<AssignedChild> {
        val assigned = TreeSet(children)
        assigned.removeAll(initialChildren)
        return assigned
    }

    private fun unassignedChildren(): Set<AssignedChild> {
        val unassigned = TreeSet(initialChildren)
        unassigned.removeAll(children)
        return unassigned
    }

    private fun changedChildren(): Set<AssignedChild> {
        val changed = TreeSet(initialChildren)
        changed.retainAll(children)
        return changed.stream()
                .filter { it.changed }
                .collect(toCollection(::TreeSet))
    }
}

internal open class PersistedParent(
        private val persisted: PersistedDomain<ParentSnapshot, ParentRecord, PersistedParentDependentDetails, PersistedParentFactory, Parent, MutableParent>)
    : Parent,
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
                "Deleting parent with assigned children: $this")

        persisted.delete()
    }

    override fun <R> update(block: MutableParent.() -> R): R =
            PersistedMutableParent(
                    persisted.record,
                    persisted.dependent.children,
                    ::addChild.uncurryFirst(),
                    ::removeChild.uncurryFirst())
                    .let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParent
        return persisted == other.persisted
    }

    override fun hashCode() = persisted.hashCode()

    override fun toString() = "${super.toString()}$persisted"

    private fun addChild(child: AssignedChild) {
        persisted.dependent.addChild(child)
    }

    private fun removeChild(child: AssignedChild) {
        persisted.dependent.removeChild(child)
    }
}

internal data class PersistedMutableParent(
        private val record: ParentRecord,
        private val initial: Set<AssignedChild>,
        private val added: (AssignedChild, MutableSet<AssignedChild>) -> Unit,
        private val removed: (AssignedChild, MutableSet<AssignedChild>) -> Unit)
    : MutableParent,
        MutableParentSimpleDetails by record {
    override val at: OffsetDateTime?
        get() = children.at
    override val sideValues =
            TrackedSortedSet(
                    record.sideValues,
                    ::replaceSideValues.uncurrySecond(),
                    ::replaceSideValues.uncurrySecond())
    override val children =
            TrackedSortedSet(
                    initial, added, removed)

    private fun replaceSideValues(all: MutableSet<String>) {
        record.sideValues = all
    }
}
