package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEvent

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
    override val version: Int
}

interface MutableParent : MutableParentDetails {
    fun save(): MutableParent

    fun delete()
}

interface Parent : ParentDetails {
    val existing: Boolean

    fun update(block: MutableParent.() -> Unit): Parent
}

data class ParentChangedEvent(
        val before: ParentResource?,
        val after: ParentResource?)
    : ApplicationEvent(after ?: before!!)
