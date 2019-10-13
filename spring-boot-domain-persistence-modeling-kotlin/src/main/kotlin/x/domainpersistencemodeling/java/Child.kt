package x.domainpersistencemodeling.java

interface Child : ChildDetails,
        ScopedMutable<Child, MutableChild>,
        UpsertableDomain<Child> {
    fun toResource(): ChildResource
}
