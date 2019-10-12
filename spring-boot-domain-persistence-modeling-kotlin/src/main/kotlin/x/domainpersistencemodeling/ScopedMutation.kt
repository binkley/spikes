package x.domainpersistencemodeling

interface ScopedMutation<Domain, Mutable> {
    /**
     * Runs [block] against [Mutable], and returns a new, updated version of
     * [Domain] which includes changes from [block], but _does not_ save to
     * persistence.
     *
     * @return an updated domain object with changes unsaved
     */
    fun update(block: Mutable.() -> Unit): Domain

    /**
     * Saves to persistence, and returns an updated domain object with any
     * changes made by persistence (eg, audit columns).
     *
     * @return an updated domain object, including changes made by persistence
     */
    fun save(): Domain

    /**
     * Deletes from persistence.  Afterwards, the domain object is _unusable_.
     */
    fun delete()
}
