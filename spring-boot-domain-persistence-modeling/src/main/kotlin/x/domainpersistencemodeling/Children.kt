package x.domainpersistencemodeling

import org.springframework.context.ApplicationEvent

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
    fun save(): MutableChild

    fun delete()

    fun addTo(parent: ParentResource): MutableChild
}

interface Child : ChildDetails {
    val existing: Boolean

    /**
     * Runs [block] against [this], and returns a new, updated version
     * of `Parent`.  Afterwards, do not reuse the original `Parent, but
     * capture the return.
     */
    fun update(block: MutableChild.() -> Unit): Child?

    fun updateAndSave(block: MutableChild.() -> Unit): Child

    fun delete()
}

data class ChildChangedEvent(
        val before: ChildResource?,
        val after: ChildResource?)
    : ApplicationEvent(after ?: before!!)
