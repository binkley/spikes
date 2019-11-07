package x.domainpersistencemodeling

import java.time.OffsetDateTime

data class ParentSnapshot(
        val naturalId: String,
        val state: String,
        val at: OffsetDateTime?,
        val value: String?,
        val sideValues: Set<String>, // Sorted
        val version: Int)

interface ParentFactory {
    fun all(): Sequence<Parent>
    fun findExisting(naturalId: String): Parent?
    fun createNew(naturalId: String): Parent
    fun findExistingOrCreateNew(naturalId: String): Parent
}

interface ParentDetails
    : Comparable<ParentDetails> {
    val naturalId: String
    val state: String
    val value: String?
    val sideValues: Set<String> // Sorted
    val version: Int

    override fun compareTo(other: ParentDetails) =
            naturalId.compareTo(other.naturalId)
}

interface MutableParentDetails : ParentDetails {
    override var state: String
    override var value: String?
    override val sideValues: MutableSet<String> // Sorted
}

interface MutableParent : MutableParentDetails {
    val children: MutableSet<AssignedChild>
}

interface Parent
    : ParentDetails,
        ScopedMutable<Parent, MutableParent>,
        PersistableDomain<ParentSnapshot, Parent> {
    val children: Set<AssignedChild>
    val at: OffsetDateTime?

    /** Assigns [child] to this parent, a mutable operation. */
    fun assign(child: UnassignedChild): AssignedChild

    /** Unassigns [child] from this parent, a mutable operation. */
    fun unassign(child: AssignedChild): UnassignedChild
}

data class ParentChangedEvent(
        val before: ParentSnapshot?,
        val after: ParentSnapshot?)
    : DomainChangedEvent<ParentSnapshot>(before, after)
