package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEvent

data class ChildResource(
        val naturalId: String,
        val parent: ParentResource?,
        val value: String?,
        val version: Int)

interface ChildFactory {
    fun all(): Sequence<Child>

    fun findExisting(naturalId: String): Child?

    fun createNew(resource: ChildResource): Child

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
    fun save(): MutableChild

    fun delete()
}

interface Child : ChildDetails {
    fun update(block: MutableChild.() -> Unit): Child

    val existing: Boolean
}

data class ChildChangedEvent(
        val before: ChildResource?,
        val after: ChildResource?)
    : ApplicationEvent(after ?: before!!)
