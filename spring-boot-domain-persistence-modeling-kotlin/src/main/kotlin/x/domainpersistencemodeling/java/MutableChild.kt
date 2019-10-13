package x.domainpersistencemodeling.java

interface MutableChild : MutableChildDetails {
    fun assignTo(parent: ParentDetails)
    fun unassignFromAny()
}
