package x.domainpersistencemodeling

import org.springframework.context.ApplicationEvent

data class ParentResource(
        val naturalId: String,
        val value: String?,
        val version: Int)

interface ParentFactory {
    fun all(): Sequence<Parent>

    fun byNaturalId(naturalId: String): Parent?

    fun new(resource: ParentResource): Parent
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
    fun update(block: MutableParent.() -> Unit): Parent
}

data class ParentChangedEvent(
        val before: ParentResource?,
        val after: ParentResource?)
    : ApplicationEvent(after ?: before!!)
