package x.domainpersistencemodeling

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

interface MutableOtherDependentDetails

interface Other
    : OtherSimpleDetails,
        OtherDependentDetails,
        ScopedMutable<Other, MutableOther>,
        PersistableDomain<OtherSnapshot, Other>

interface MutableOther
    : MutableOtherSimpleDetails,
        MutableOtherDependentDetails

data class OtherChangedEvent(
        val before: OtherSnapshot?,
        val after: OtherSnapshot?)
    : DomainChangedEvent<OtherSnapshot>(before, after)
