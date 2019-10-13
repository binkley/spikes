package x.domainpersistencemodeling.java

import java.util.stream.Stream

interface ChildFactory {
    fun all(): Stream<Child>
    fun findExisting(naturalId: String): Child?
    fun createNew(naturalId: String): Child
    fun findExistingOrCreateNew(naturalId: String): Child
    fun byParentNaturalId(parentNaturalId: String): Sequence<Child>
}
