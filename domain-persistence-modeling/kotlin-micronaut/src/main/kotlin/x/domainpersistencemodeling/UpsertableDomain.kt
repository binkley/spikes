package x.domainpersistencemodeling

interface UpsertableDomain<Domain : UpsertableDomain<Domain>> {
    val version: Int

    val existing: Boolean
        get() = 0 < version

    /**
     * Optimizes to lessen the number of persistence calls.  The code should
     * be correct with or without this optimization: for example, a domain
     * object version number should have the same values.
     */
    val changed: Boolean

    fun save(): UpsertedDomainResult<Domain>

    fun delete()

    data class UpsertedDomainResult<Domain : UpsertableDomain<Domain>>(
            val domain: Domain, val changed: Boolean)
}
