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

interface OtherIntrinsicDetails
    : Comparable<OtherIntrinsicDetails> {
    val naturalId: String
    val value: String?
    val version: Int

    override fun compareTo(other: OtherIntrinsicDetails) =
            naturalId.compareTo(other.naturalId)
}

interface OtherComputedDetails

interface MutableOtherIntrinsicDetails : OtherIntrinsicDetails {
    override var value: String?
}

interface MutableOtherComputedDetails

interface Other
    : OtherIntrinsicDetails,
        OtherComputedDetails,
        ScopedMutable<Other, MutableOther>,
        PersistableDomain<OtherSnapshot, Other>

interface MutableOther
    : MutableOtherIntrinsicDetails,
        MutableOtherComputedDetails

data class OtherChangedEvent(
        val before: OtherSnapshot?,
        val after: OtherSnapshot?)
    : DomainChangedEvent<OtherSnapshot>(before, after)
