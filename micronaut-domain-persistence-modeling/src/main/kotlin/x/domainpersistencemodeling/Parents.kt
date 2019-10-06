package x.domainpersistencemodeling

data class ParentResource(
        val naturalId: String,
        val value: String?,
        val version: Int)

interface ParentFactory {
    fun all(): Sequence<Parent>
    fun findExisting(naturalId: String): Parent?
    fun createNew(resource: ParentResource): Parent
    fun findExistingOrCreateNew(naturalId: String): Parent
}

interface ParentDetails {
    val naturalId: String
    val value: String?
    val version: Int
}

interface MutableParentDetails
    : ParentDetails {
    override val naturalId: String
    override var value: String?
}

interface MutableParent : MutableParentDetails

interface Parent : ParentDetails,
        ScopedMutation<Parent, MutableParent>,
        Persisted {
    fun toResource(): ParentResource
}

data class ParentChangedEvent(
        val before: ParentResource?,
        val after: ParentResource?)
    : DomainChangedEvent<ParentResource>(before, after)
