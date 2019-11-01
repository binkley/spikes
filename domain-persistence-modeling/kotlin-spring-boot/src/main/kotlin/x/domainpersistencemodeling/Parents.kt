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
    val children: MutableSet<Child>

    fun assign(child: Child) {
        if (!children.add(child))
            throw DomainException("Already assigned: $child")
    }

    fun unassign(child: Child) {
        if (!children.remove(child))
            throw DomainException("Not assigned: $child")
    }
}

interface Parent
    : ParentDetails,
        ScopedMutable<Parent, MutableParent>,
        PersistableDomain<ParentResource, Parent> {
    val children: Set<Child>

    /**
     * Assigns [child] to this parent, a mutable operation, and [save]s
     * parent and child.
     */
    fun assign(child: Child): Parent

    /**
     * Unassigns [child] from this parent, a mutable operation, and [save]s
     * parent and child.
     */
    fun unassign(child: Child): Parent
}

data class ParentChangedEvent(
        val before: ParentResource?,
        val after: ParentResource?)
    : DomainChangedEvent<ParentResource>(before, after)
