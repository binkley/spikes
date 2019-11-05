package x.domainpersistencemodeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest
@Transactional
internal open class SideValuesTest @Autowired constructor(
        private val testing: Testing) {
    @Test
    internal fun `should use defaults for children`() {
        val child = testing.createNewUnassignedChild()
        child.update {
            defaultSideValues.add("A")
        }

        assertThat(child.currentSideValues).isEqualTo(setOf("A"))
    }

    @Test
    internal fun `should use overrides for children`() {
        val child = testing.createNewUnassignedChild()
        child.update {
            defaultSideValues.add("A")
            sideValues.add("B")
        }

        assertThat(child.currentSideValues).isEqualTo(setOf("B"))
    }

    @Test
    internal fun `should use children's intersection for parents`() {
        val parent = testing.createNewParent()
        val childA = testing.createNewUnassignedChild("P")
        childA.update {
            defaultSideValues.addAll(setOf("A", "B"))
        }
        parent.assign(childA)
        val childB = testing.createNewUnassignedChild("Q")
        childB.update {
            defaultSideValues.addAll(setOf("A", "C"))
        }
        parent.assign(childB)
        // Child "C" has no side values: should be ignored
        val childC = testing.createNewUnassignedChild("R")
        parent.assign(childC)

        assertThat(parent.currentSideValues).isEqualTo(setOf("A"))
    }

    @Test
    internal fun `should use overrides for parents`() {
        val parent = testing.createNewParent()
        parent.update {
            sideValues.add("A")
        }
        val child = testing.createNewUnassignedChild()
        child.update {
            defaultSideValues.add("B")
        }
        parent.assign(child)

        assertThat(parent.currentSideValues).isEqualTo(setOf("A"))
    }
}
