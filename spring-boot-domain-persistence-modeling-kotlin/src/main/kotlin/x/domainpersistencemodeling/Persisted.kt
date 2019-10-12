package x.domainpersistencemodeling

interface Persisted {
    /** Checks if this domain object is present in persistence. */
    val existing: Boolean
    val version: Int
}
