package x.domainpersistencemodeling

import java.time.OffsetDateTime

data class ChildSnapshot(
        val naturalId: String,
        val otherNaturalId: String?,
        val parentNaturalId: String?,
        val state: String,
        val at: OffsetDateTime, // UTC
        val value: String?,
        val sideValues: Set<String>, // Sorted
        val version: Int)

interface ChildFactory {
    fun all(): Sequence<Child<*>>
    fun findExisting(naturalId: String): Child<*>?
    fun createNewUnassigned(naturalId: String): UnassignedChild
    fun findExistingOrCreateNewUnassigned(naturalId: String): Child<*>
    fun findAssignedFor(parentNaturalId: String): Sequence<AssignedChild>
}

interface ChildIntrinsicDetails
    : Comparable<ChildIntrinsicDetails> {
    val naturalId: String
    val otherNaturalId: String? // TODO: Not yet mutable
    val parentNaturalId: String?
    val state: String
    val at: OffsetDateTime // UTC
    val value: String?
    val sideValues: Set<String> // Sorted
    val defaultSideValues: Set<String> // Sorted
    val version: Int

    val assigned: Boolean
        get() = null != parentNaturalId

    val relevant: Boolean
        get() = KnownState.forName(state)?.relevant ?: true

    override operator fun compareTo(other: ChildIntrinsicDetails) =
            naturalId.compareTo(other.naturalId)
}

interface ChildComputedDetails

interface MutableChildIntrinsicDetails : ChildIntrinsicDetails {
    override var parentNaturalId: String?
    override var state: String
    override var at: OffsetDateTime // UTC
    override var value: String?
    override val sideValues: MutableSet<String> // Sorted
    override val defaultSideValues: MutableSet<String> // Sorted
}

interface MutableChildComputedDetails

interface Child<C : Child<C>>
    : ChildIntrinsicDetails,
        ChildComputedDetails,
        ScopedMutable<C, MutableChild>,
        PersistableDomain<ChildSnapshot, C>

interface MutableChild
    : MutableChildIntrinsicDetails,
        MutableChildComputedDetails

interface UnassignedChild : Child<UnassignedChild> {
    /** Assigns this child to [parent], a mutable operation. */
    fun assignTo(parent: Parent): AssignedChild
}

interface AssignedChild : Child<AssignedChild> {
    /** Unassigns this child from any parent, a mutable operation. */
    fun unassignFromAny(): UnassignedChild
}

data class ChildChangedEvent(
        val before: ChildSnapshot?,
        val after: ChildSnapshot?)
    : DomainChangedEvent<ChildSnapshot>(before, after)
