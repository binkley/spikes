package x.domainpersistencemodeling

import org.springframework.context.ApplicationEvent

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

    /**
     * Runs [block] against [this], and returns a new, updated version
     * of `Parent`.  Afterwards, do not reuse the original `Parent, but
     * capture the return.
     */
    fun update(block: MutableParent.() -> Unit): Parent?

    fun updateAndSave(block: MutableParent.() -> Unit): Parent

    fun delete()
}

data class ParentChangedEvent(
        val before: ParentResource?,
        val after: ParentResource?)
    : ApplicationEvent(after ?: before!!)
