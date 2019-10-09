package x.domainpersistencemodeling

data class ChildResource(
        val naturalId: String,
        val parentNaturalId: String?,
        val value: String?,
        val subchildren: Set<String>, // Sorted
        val version: Int)

interface ChildFactory {
    fun all(): Sequence<Child>
    fun findExisting(naturalId: String): Child?
    fun createNew(naturalId: String): Child
    fun findExistingOrCreateNew(naturalId: String): Child
    fun findOwned(parentNaturalId: String): Iterable<Child>
}

interface ChildPersistedDetails {
    val naturalId: String
    val parentId: Long?
    val value: String?
    val subchildren: Set<String>
    val version: Int
}

interface MutableChildDetails
    : ChildPersistedDetails {
    override val naturalId: String
    override var parentId: Long?
    override var value: String?
    override val subchildren: MutableSet<String>
}

interface MutableChild : MutableChildDetails {
    fun assignTo(parent: Parent)
    fun unassignFromAny()
}

interface Child : ScopedMutation<Child, MutableChild>,
        Comparable<Child>,
        Persisted {
    val naturalId: String
    val parentNaturalId: String?
    val value: String?
    val subchildren: Set<String> // Sorted
}

data class ChildChangedEvent(
        val before: ChildResource?,
        val after: ChildResource?)
    : DomainChangedEvent<ChildResource>(before, after)
