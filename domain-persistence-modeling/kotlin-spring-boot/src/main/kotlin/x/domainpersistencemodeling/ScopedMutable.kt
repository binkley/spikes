package x.domainpersistencemodeling

interface ScopedMutable<Mutable> {
    /**
     * Runs [block] against [Mutable], and returns a new, updated version of
     * [Domain] which includes changes from [block], but _does not_ save to
     * persistence.
     *
     * @return an updated domain object with changes unsaved
     */
    fun <R> update(block: Mutable.() -> R): R
}
