package x.domainpersistencemodeling

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest
@Transactional
internal open class DomainTest @Autowired constructor(
        private val parents: ParentFactory,
        private val children: ChildFactory) {
    companion object {
        const val parentNaturalId = "a"
        const val childNaturalId = "p"
    }

    fun newUnsavedParent() = parents.createNew(parentNaturalId)
    fun newUnsavedChild() = children.createNew(childNaturalId)

    @Test
    fun shouldAssignAndUnassignChild() {
        val parent = newUnsavedParent()
        parent.save()
        val child = newUnsavedChild()
        child.save()

        parent.assign(child)

        parent.unassign(child)
    }
}
