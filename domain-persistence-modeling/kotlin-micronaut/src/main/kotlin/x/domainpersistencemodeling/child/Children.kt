package x.domainpersistencemodeling.child

import x.domainpersistencemodeling.DomainChangedEvent
import x.domainpersistencemodeling.ScopedMutable
import x.domainpersistencemodeling.UpsertableDomain
import x.domainpersistencemodeling.parent.ParentDetails

data class ChildResource(
        val naturalId: String,
        val parentNaturalId: String?,
        val value: String?,
        val subchildren: Set<String>, // Sorted
        val version: Int)

interface ChildFactory {
    fun all(): Sequence<Child>
    fun findExisting(naturalId: String): Child?
    fun createNew(naturalId: String): Child
    fun findExistingOrCreateNew(naturalId: String): Child
    fun findOwned(parentNaturalId: String): Sequence<Child>
}

interface ChildDetails : Comparable<ChildDetails> {
    val naturalId: String
    val parentNaturalId: String?
    val value: String?
    val subchildren: Set<String>
    val version: Int

    override operator fun compareTo(other: ChildDetails) =
            naturalId.compareTo(other.naturalId)
}

interface MutableChildDetails : ChildDetails {
    override var parentNaturalId: String?
    override var value: String?
    override val subchildren: MutableSet<String>
}

interface MutableChild : MutableChildDetails {
    fun assignTo(parent: ParentDetails)
    fun unassignFromAny()
}

interface Child : ChildDetails,
        ScopedMutable<Child, MutableChild>,
        UpsertableDomain<Child> {
    fun toResource(): ChildResource
}

data class ChildChangedEvent(
        val before: ChildResource?,
        val after: ChildResource?)
    : DomainChangedEvent<ChildResource>(before, after)
