package x.domainpersistencemodeling.java

interface ParentFactory {
    fun all(): Sequence<Parent>
    fun findExisting(naturalId: String): Parent?
    fun createNew(naturalId: String): Parent
    fun findExistingOrCreateNew(naturalId: String): Parent
}
