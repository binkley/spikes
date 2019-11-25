package x.domainpersistencemodeling

interface PersistableDomain<Snapshot,
        Domain : PersistableDomain<Snapshot, Domain>> {
    val naturalId: String

    val version: Int

    val existing: Boolean
        get() = 0 < version

    /**
     * Optimizes to lessen the count of persistence calls.  The code should
     * be correct with or without this optimization: for example, a domain
     * object version number should have the same values.
     */
    val changed: Boolean

    fun save(): UpsertedDomainResult<Snapshot, Domain>

    fun delete()

    data class UpsertedDomainResult<Snapshot,
            Domain : PersistableDomain<Snapshot, Domain>>(
        val domain: Domain, val changed: Boolean
    )
}
