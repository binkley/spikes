package x.domainpersistencemodeling

interface PersistableDomain<Resource,
        Domain : PersistableDomain<Resource, Domain>> {
    val version: Int

    val existing: Boolean
        get() = 0 < version

    /**
     * Optimizes to lessen the count of persistence calls.  The code should
     * be correct with or without this optimization: for example, a domain
     * object version number should have the same values.
     */
    val changed: Boolean

    fun save(): UpsertedDomainResult<Resource, Domain>

    fun delete()

    fun toResource(): Resource

    data class UpsertedDomainResult<Resource,
            Domain : PersistableDomain<Resource, Domain>>(
            val domain: Domain, val changed: Boolean)
}
