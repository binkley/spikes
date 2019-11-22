package x.domainpersistencemodeling.other

import x.domainpersistencemodeling.DomainChangedEvent
import x.domainpersistencemodeling.PersistableDomain
import x.domainpersistencemodeling.ScopedMutable

data class OtherSnapshot(
        val naturalId: String,
        val value: String?,
        val version: Int)

interface OtherFactory {
    fun all(): Sequence<Other>
    fun findExisting(naturalId: String): Other?
    fun createNew(naturalId: String): Other
    fun findExistingOrCreateNew(naturalId: String): Other
}

interface OtherSimpleDetails
    : Comparable<OtherSimpleDetails> {
    val naturalId: String
    val value: String?
    val version: Int

    override fun compareTo(other: OtherSimpleDetails) =
            naturalId.compareTo(other.naturalId)
}

interface OtherDependentDetails

interface MutableOtherSimpleDetails : OtherSimpleDetails {
    override var value: String?
}

interface MutableOtherDependentDetails : OtherDependentDetails

interface Other
    : OtherSimpleDetails,
        OtherDependentDetails,
        ScopedMutable<MutableOther>,
        PersistableDomain<OtherSnapshot, Other>

interface MutableOther
    : MutableOtherSimpleDetails,
        MutableOtherDependentDetails

data class OtherChangedEvent(
        val before: OtherSnapshot?,
        val after: OtherSnapshot?)
    : DomainChangedEvent<OtherSnapshot>(before, after)
