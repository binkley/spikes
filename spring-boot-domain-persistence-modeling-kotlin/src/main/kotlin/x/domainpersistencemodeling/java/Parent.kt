package x.domainpersistencemodeling.java

interface Parent : ParentDetails,
        ScopedMutable<Parent?, MutableParent?>,
        UpsertableDomain<Parent> {
    // Immutable
    val children: Set<Child>

    fun toResource(): ParentResource
}
