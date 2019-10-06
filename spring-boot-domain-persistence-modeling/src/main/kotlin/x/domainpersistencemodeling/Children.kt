package x.domainpersistencemodeling

data class ChildResource(
        val naturalId: String,
        val parent: ParentResource?,
        val value: String?,
        val version: Int)

interface ChildFactory {
    fun all(): Sequence<Child>
    fun findExisting(naturalId: String): Child?
    fun createNew(naturalId: String): Child
    fun findExistingOrCreateNew(naturalId: String): Child
}

interface ChildDetails {
    val naturalId: String
    val parentId: Long?
    val value: String?
    val version: Int
}

interface MutableChildDetails
    : ChildDetails {
    override val naturalId: String
    override var parentId: Long?
    override var value: String?
    override val version: Int
}

interface MutableChild : MutableChildDetails {
    fun addTo(parent: ParentResource): MutableChild
}

interface Child : ChildDetails,
        ScopedMutation<Child, MutableChild>,
        Persisted

data class ChildChangedEvent(
        val before: ChildResource?,
        val after: ChildResource?)
    : DomainChangedEvent<ChildResource>(before, after)
