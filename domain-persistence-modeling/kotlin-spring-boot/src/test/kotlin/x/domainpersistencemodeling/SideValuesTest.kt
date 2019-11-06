package x.domainpersistencemodeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.KnownState.DISABLED

internal class SideValuesTest
    : LiveTestBase() {
    @Test
    internal fun `should use defaults for children`() {
        val child = createNewUnassignedChild()
        child.update {
            defaultSideValues.add("A")
        }

        assertThat(child.currentSideValues).isEqualTo(setOf("A"))
    }

    @Test
    internal fun `should ignore defaults for disabled children`() {
        val child = createNewUnassignedChild()
        child.update {
            defaultSideValues.add("A")
            state = DISABLED.name
        }

        assertThat(child.currentSideValues).isEmpty()
    }

    @Test
    internal fun `should use overrides for children`() {
        val child = createNewUnassignedChild()
        child.update {
            defaultSideValues.add("A")
            sideValues.add("B")
        }

        assertThat(child.currentSideValues).isEqualTo(setOf("B"))
    }

    @Test
    internal fun `should ignore overrides for disabled children`() {
        val child = createNewUnassignedChild()
        child.update {
            defaultSideValues.add("A")
            sideValues.add("B")
            state = DISABLED.name
        }

        assertThat(child.currentSideValues).isEmpty()
    }

    @Test
    internal fun `should use children's intersection for parents`() {
        val parent = createNewParent()
        val childA = createNewUnassignedChild("P")
        childA.update {
            defaultSideValues.addAll(setOf("A", "B"))
        }
        parent.assign(childA)
        val childB = createNewUnassignedChild("Q")
        childB.update {
            defaultSideValues.addAll(setOf("A", "C"))
        }
        parent.assign(childB)
        // Child "C" has no side values: should be ignored
        val childC = createNewUnassignedChild("R")
        parent.assign(childC)

        assertThat(parent.currentSideValues).isEqualTo(setOf("A"))
    }

    @Test
    internal fun `should use overrides for parents`() {
        val parent = createNewParent()
        parent.update {
            sideValues.add("A")
        }
        val child = createNewUnassignedChild()
        child.update {
            defaultSideValues.add("B")
        }
        parent.assign(child)

        assertThat(parent.currentSideValues).isEqualTo(setOf("A"))
    }
}
