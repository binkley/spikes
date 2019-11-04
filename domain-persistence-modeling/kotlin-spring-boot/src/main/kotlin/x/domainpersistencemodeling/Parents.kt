package x.domainpersistencemodeling

data class ParentResource(
        val naturalId: String,
        val value: String?,
        val version: Int)

interface ParentFactory {
    fun all(): Sequence<Parent>
    fun findExisting(naturalId: String): Parent?
    fun createNew(naturalId: String): Parent
    fun findExistingOrCreateNew(naturalId: String): Parent
}

interface ParentDetails : Comparable<ParentDetails> {
    val naturalId: String
    val value: String?
    val version: Int

    override fun compareTo(other: ParentDetails) =
            naturalId.compareTo(other.naturalId)
}

interface MutableParentDetails : ParentDetails {
    override var value: String?
}

interface MutableParent : MutableParentDetails {
    val children: MutableSet<AssignedChild>
}

interface Parent
    : ParentDetails,
        ScopedMutable<Parent, MutableParent>,
        PersistableDomain<ParentResource, Parent> {
    val children: Set<AssignedChild>

    /** Assigns [child] to this parent, a mutable operation. */
    fun assign(child: UnassignedChild): AssignedChild

    /** Unassigns [child] from this parent, a mutable operation. */
    fun unassign(child: AssignedChild): UnassignedChild
}

data class ParentChangedEvent(
        val before: ParentResource?,
        val after: ParentResource?)
    : DomainChangedEvent<ParentResource>(before, after)
