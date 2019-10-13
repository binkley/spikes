package x.domainpersistencemodeling.java

data class ChildResource(
        val naturalId: String,
        val parentNaturalId: String?,
        val value: String?,
        val subchildren: Set<String>,
        val version: Int)
