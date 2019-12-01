package x.domainpersistencemodeling

interface ScopedMutable<Mutable> {
    /** Runs [block] against [Mutable], and returns the [block] result. */
    fun <R> update(block: Mutable.() -> R): R
}
