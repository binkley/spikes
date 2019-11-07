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

interface ParentIntrinsicDetails
    : Comparable<ParentIntrinsicDetails> {
    val naturalId: String
    val state: String
    val value: String?
    val sideValues: Set<String> // Sorted
    val version: Int

    override fun compareTo(other: ParentIntrinsicDetails) =
            naturalId.compareTo(other.naturalId)
}

interface ParentComputedDetails {
    val children: Set<AssignedChild>
    val at: OffsetDateTime?
}

interface MutableParentIntrinsicDetails : ParentIntrinsicDetails {
    override var state: String
    override var value: String?
    override val sideValues: MutableSet<String> // Sorted
}

interface MutableParentComputedDetails {
    val children: MutableSet<AssignedChild>
}

interface Parent
    : ParentIntrinsicDetails,
        ParentComputedDetails,
        ScopedMutable<Parent, MutableParent>,
        PersistableDomain<ParentSnapshot, Parent> {
    /** Assigns [child] to this parent, a mutable operation. */
    fun assign(child: UnassignedChild): AssignedChild

    /** Unassigns [child] from this parent, a mutable operation. */
    fun unassign(child: AssignedChild): UnassignedChild
}

interface MutableParent
    : MutableParentIntrinsicDetails,
        MutableParentComputedDetails

data class ParentChangedEvent(
        val before: ParentSnapshot?,
        val after: ParentSnapshot?)
    : DomainChangedEvent<ParentSnapshot>(before, after)
