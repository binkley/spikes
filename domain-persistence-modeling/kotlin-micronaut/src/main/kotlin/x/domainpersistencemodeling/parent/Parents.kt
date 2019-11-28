package x.domainpersistencemodeling.parent

import x.domainpersistencemodeling.DomainChangedEvent
import x.domainpersistencemodeling.PersistableDomain
import x.domainpersistencemodeling.ScopedMutable
import x.domainpersistencemodeling.child.AssignedChild
import x.domainpersistencemodeling.child.ChildSimpleDetails
import x.domainpersistencemodeling.child.UnassignedChild
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.other.OtherSimpleDetails
import java.time.OffsetDateTime
import java.util.*

data class ParentSnapshot(
    val naturalId: String,
    val otherNaturalId: String?,
    val state: String,
    val at: OffsetDateTime?,
    val value: String?,
    val sideValues: Set<String>, // Sorted
    val version: Int
)

interface ParentRepository {
    fun findAll(): Iterable<ParentRecord>
    fun findByNaturalId(naturalId: String): Optional<ParentRecord>
    fun upsert(entity: ParentRecord): Optional<ParentRecord>
    fun delete(entity: ParentRecord)
}

interface ParentFactory {
    fun all(): Sequence<Parent>
    fun findExisting(naturalId: String): Parent?
    fun createNew(naturalId: String): Parent
    fun findExistingOrCreateNew(naturalId: String): Parent
}

interface ParentSimpleDetails
    : Comparable<ParentSimpleDetails> {
    val naturalId: String
    val otherNaturalId: String?
    val state: String
    val value: String?
    val sideValues: Set<String> // Sorted
    val version: Int

    override fun compareTo(other: ParentSimpleDetails) =
        naturalId.compareTo(other.naturalId)
}

interface ParentDependentDetails {
    val other: OtherSimpleDetails?
    val children: Set<ChildSimpleDetails>
    val at: OffsetDateTime?
}

interface MutableParentSimpleDetails : ParentSimpleDetails {
    override var otherNaturalId: String?
    override var state: String
    override var value: String?
    override val sideValues: MutableSet<String> // Sorted
}

interface MutableParentDependentDetails
    : ParentDependentDetails {
    override var other: Other?
    override val children: MutableSet<AssignedChild>
}

interface Parent
    : ParentSimpleDetails,
    ParentDependentDetails,
    ScopedMutable<MutableParent>,
    PersistableDomain<ParentSnapshot, Parent> {
    override val other: Other?
    override val children: Set<AssignedChild>

    /** Assigns [other] to this parent, a mutable operation. */
    fun assign(other: Other)

    /** Unassigns any other from this parent, a mutable operation. */
    fun unassignAnyOther()

    /** Assigns [child] to this parent, a mutable operation. */
    fun assign(child: UnassignedChild): AssignedChild

    /** Unassigns [child] from this parent, a mutable operation. */
    fun unassign(child: AssignedChild): UnassignedChild
}

interface MutableParent
    : MutableParentSimpleDetails,
    MutableParentDependentDetails

data class ParentChangedEvent(
    val before: ParentSnapshot?,
    val after: ParentSnapshot?
) : DomainChangedEvent<ParentSnapshot>(before, after)
