package x.domainpersistencemodeling

data class ChildResource(
        val naturalId: String,
        val parentNaturalId: String?,
        val value: String?,
        val sideValues: Set<String>, // Sorted
        val version: Int)
    : SideValued

interface ChildFactory {
    fun all(): Sequence<Child>
    fun findExisting(naturalId: String): Child?
    fun createNew(naturalId: String): UnassignedChild
    fun findExistingOrCreateNew(naturalId: String): Child
    fun findOwned(parentNaturalId: String): Sequence<AssignedChild>
}

interface ChildDetails
    : SideValued,
        Comparable<ChildDetails> {
    val naturalId: String
    val parentNaturalId: String?
    val value: String?
    val sideValues: Set<String> // Sorted
    val version: Int

    val assigned: Boolean
        get() = null != parentNaturalId

    override operator fun compareTo(other: ChildDetails) =
            naturalId.compareTo(other.naturalId)
}

interface MutableChildDetails : ChildDetails {
    override var parentNaturalId: String?
    override var value: String?
    override val sideValues: MutableSet<String> // Sorted
}

interface MutableChild : MutableChildDetails {
    fun assignTo(parent: ParentDetails)
    fun unassignFromAny()
}

interface Child : ChildDetails,
        ScopedMutable<Child, MutableChild>,
        PersistableDomain<ChildResource, Child>

interface UnassignedChild : Child {
    /** Assigns this child to [parent], a mutable operation. */
    fun assignTo(parent: Parent): AssignedChild
}

interface AssignedChild : Child {
    /** Unassigns this child from any parent, a mutable operation. */
    fun unassignFromAny(): UnassignedChild
}

data class ChildChangedEvent(
        val before: ChildResource?,
        val after: ChildResource?)
    : DomainChangedEvent<ChildResource>(before, after)
