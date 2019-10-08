package x.domainpersistencemodeling

import java.util.*

data class ChildResource(
        val naturalId: String,
        val parentNaturalId: String?,
        val value: String?,
        val subchildren: SortedSet<String>,
        val version: Int)

interface ChildFactory {
    fun all(): Sequence<Child>
    fun findExisting(naturalId: String): Child?
    fun createNew(naturalId: String): Child
    fun findExistingOrCreateNew(naturalId: String): Child
}

interface ChildPersistedDetails {
    val naturalId: String
    val parentId: Long?
    val value: String?
    val subchildJson: String
    val version: Int
}

interface MutableChildDetails
    : ChildPersistedDetails {
    override val naturalId: String
    override var parentId: Long?
    override var value: String? // TODO: List<String>
    override var subchildJson: String
}

interface MutableChild : MutableChildDetails {
    val subchildren: MutableSet<String>
    fun addTo(parent: ParentResource): MutableChild
}

interface Child : ScopedMutation<Child, MutableChild>,
        Persisted {
    val naturalId: String
    val parentNaturalId: String?
    val value: String?
    val subchildren: SortedSet<String>
}

data class ChildChangedEvent(
        val before: ChildResource?,
        val after: ChildResource?)
    : DomainChangedEvent<ChildResource>(before, after)
