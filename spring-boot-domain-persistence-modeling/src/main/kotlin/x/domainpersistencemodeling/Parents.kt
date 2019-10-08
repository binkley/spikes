package x.domainpersistencemodeling

import java.util.*

data class ParentResource(
        val naturalId: String,
        val value: String?,
        val version: Int)

interface ParentFactory {
    fun all(): Sequence<Parent>
    fun findExisting(naturalId: String): Parent?
    fun createNew(naturalId: String): Parent
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
    val children: SortedSet<Child>

    fun toResource(): ParentResource
}

data class ParentChangedEvent(
        val before: ParentResource?,
        val after: ParentResource?)
    : DomainChangedEvent<ParentResource>(before, after)
