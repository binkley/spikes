package x.domainpersistencemodeling.java

interface ChildDetails : Comparable<Child> {
    val naturalId: String
    val parentNaturalId: String?
    val value: String?
    val subchildren: Set<String>
    val version: Int

    override operator fun compareTo(other: Child): Int {
        return naturalId.compareTo(other.naturalId)
    }
}
