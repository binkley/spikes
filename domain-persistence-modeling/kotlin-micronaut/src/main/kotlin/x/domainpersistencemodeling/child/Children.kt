package x.domainpersistencemodeling.child

import lombok.Generated
import x.domainpersistencemodeling.DomainChangedEvent
import x.domainpersistencemodeling.DomainDetails
import x.domainpersistencemodeling.PersistableDomain
import x.domainpersistencemodeling.ScopedMutable
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.other.OtherSimpleDetails
import java.time.OffsetDateTime
import java.util.Optional

@Generated // Lie to JaCoCo -- it misses that this is a data class
data class ChildSnapshot(
    override val naturalId: String,
    val otherNaturalId: String?,
    val parentNaturalId: String?,
    val state: String,
    val at: OffsetDateTime, // UTC
    val value: String?,
    val defaultSideValues: Set<String>, // Sorted
    val sideValues: Set<String>, // Sorted
    override val version: Int
) : DomainDetails

interface ChildRepository {
    fun findAll(): Iterable<ChildRecord>
    fun findByNaturalId(naturalId: String): Optional<ChildRecord>
    fun findByParentNaturalId(parentNaturalId: String)
            : Iterable<ChildRecord>

    fun upsert(entity: ChildRecord): Optional<ChildRecord>
    fun delete(entity: ChildRecord)
}

interface ChildFactory {
    fun all(): Sequence<Child<*>>
    fun findExisting(naturalId: String): Child<*>?
    fun createNewUnassigned(naturalId: String): UnassignedChild
    fun findExistingOrCreateNewUnassigned(naturalId: String): Child<*>
    fun findAssignedTo(parentNaturalId: String): Sequence<AssignedChild>
}

interface ChildSimpleDetails
    : Comparable<ChildSimpleDetails> {
    val naturalId: String
    val parentNaturalId: String?
    val state: String
    val at: OffsetDateTime // UTC
    val value: String?
    val defaultSideValues: Set<String> // Sorted
    val sideValues: Set<String> // Sorted
    val version: Int

    val assigned: Boolean
        get() = null != parentNaturalId

    override operator fun compareTo(other: ChildSimpleDetails) =
        naturalId.compareTo(other.naturalId)
}

interface ChildDependentDetails {
    val other: OtherSimpleDetails?
}

interface MutableChildSimpleDetails
    : ChildSimpleDetails {
    override var parentNaturalId: String?
    override var state: String
    override var at: OffsetDateTime // UTC
    override var value: String?
    override val defaultSideValues: MutableSet<String> // Sorted
    override val sideValues: MutableSet<String> // Sorted
}

interface MutableChildDependentDetails
    : ChildDependentDetails {
    override var other: Other?
}

interface Child<C : Child<C>>
    : ChildSimpleDetails,
    ChildDependentDetails,
    ScopedMutable<MutableChild>,
    PersistableDomain<ChildSnapshot, C> {
    override val other: Other?

    /** Assigns [other] to this child, a mutable operation. */
    fun assign(other: Other)

    /** Unassigns any other from this child, a mutable operation. */
    fun unassignAnyOther()
}

interface MutableChild
    : MutableChildSimpleDetails,
    MutableChildDependentDetails

interface UnassignedChild : Child<UnassignedChild>

interface AssignedChild : Child<AssignedChild>

@Generated // Lie to JaCoCo -- it misses that this is a data class
data class ChildChangedEvent(
    val before: ChildSnapshot?,
    val after: ChildSnapshot?
) : DomainChangedEvent<ChildSnapshot>(before, after)
