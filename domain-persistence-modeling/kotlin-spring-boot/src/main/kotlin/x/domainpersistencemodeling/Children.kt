package x.domainpersistencemodeling

import java.time.OffsetDateTime

data class ChildSnapshot(
        val naturalId: String,
        val parentNaturalId: String?,
        val state: String,
        val at: OffsetDateTime, // UTC
        val value: String?,
        val sideValues: Set<String>, // Sorted
        val version: Int)

interface ChildFactory {
    fun all(): Sequence<Child>
    fun findExisting(naturalId: String): Child?
    fun createNewUnassigned(naturalId: String): UnassignedChild
    fun findExistingOrCreateNewUnassigned(naturalId: String): Child
    fun findAssignedFor(parentNaturalId: String): Sequence<AssignedChild>
}

interface ChildIntrinsicDetails
    : Comparable<ChildIntrinsicDetails> {
    val naturalId: String
    val parentNaturalId: String?
    val state: String
    val at: OffsetDateTime // UTC
    val value: String?
    val sideValues: Set<String> // Sorted
    val defaultSideValues: Set<String> // Sorted
    val version: Int

    val assigned: Boolean
        get() = null != parentNaturalId

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

interface Child
    : ChildIntrinsicDetails,
        ChildComputedDetails,
        ScopedMutable<Child, MutableChild>,
        PersistableDomain<ChildSnapshot, Child> {
    val relevant: Boolean
}

/**
 * Lacks a way to restrict access to [assignTo] and [unassignFromAny] so that
 * only [Parent] implementations can use them.  Neither Java nor Kotlin have
 * something like C++'s `friend` access specifier.
 */
interface MutableChild
    : MutableChildIntrinsicDetails,
        MutableChildComputedDetails {
    fun assignTo(parent: ParentIntrinsicDetails)
    fun unassignFromAny()
}

interface UnassignedChild : Child {
    /** Assigns this child to [parent], a mutable operation. */
    fun assignTo(parent: Parent): AssignedChild
}

interface AssignedChild : Child {
    /** Unassigns this child from any parent, a mutable operation. */
    fun unassignFromAny(): UnassignedChild
}

data class ChildChangedEvent(
        val before: ChildSnapshot?,
        val after: ChildSnapshot?)
    : DomainChangedEvent<ChildSnapshot>(before, after)
