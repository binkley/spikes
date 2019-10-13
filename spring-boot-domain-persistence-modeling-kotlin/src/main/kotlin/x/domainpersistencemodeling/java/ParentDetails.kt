package x.domainpersistencemodeling.java

interface ParentDetails : Comparable<Parent> {
    val naturalId: String
    val value: String?
    val version: Int

    override fun compareTo(other: Parent): Int {
        return naturalId.compareTo(other.naturalId)
    }
}
