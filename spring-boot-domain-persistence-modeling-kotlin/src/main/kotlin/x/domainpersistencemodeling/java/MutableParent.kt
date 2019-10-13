package x.domainpersistencemodeling.java

interface MutableParent : MutableParentDetails {
    // Mutable
    val children: Set<Child>
}
