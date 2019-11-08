package x.domainpersistencemodeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.KnownState.DISABLED
import x.domainpersistencemodeling.KnownState.ENABLED

internal class SideValuesTest {
    @Test
    internal fun `should use defaults for children`() {
        val child = childHavingSideValues(setOf("A"))

        assertThat(child.currentSideValues).isEqualTo(setOf("A"))
    }

    @Test
    internal fun `should ignore defaults for disabled children`() {
        val child = childHavingSideValues(setOf("A"), setOf(),
                DISABLED.name)

        assertThat(child.currentSideValues).isEmpty()
    }

    @Test
    internal fun `should use overrides for children`() {
        val child = childHavingSideValues(setOf("A"), setOf("B"))

        assertThat(child.currentSideValues).isEqualTo(setOf("B"))
    }

    @Test
    internal fun `should ignore overrides for disabled children`() {
        val child = childHavingSideValues(setOf("B"), setOf("A"),
                DISABLED.name)

        assertThat(child.currentSideValues).isEmpty()
    }

    @Test
    internal fun `should use children's intersection for parents`() {
        val parent = parentHavingSideValues(setOf(
                childHavingSideValues(setOf("A", "B")),
                childHavingSideValues(setOf("A", "C")),
                childHavingSideValues(setOf())))

        assertThat(parent.currentSideValues).isEqualTo(setOf("A"))
    }

    @Test
    internal fun `should use overrides for parents`() {
        val parent = parentHavingSideValues(
                setOf(childHavingSideValues(setOf("B"))), setOf("A"))

        assertThat(parent.currentSideValues).isEqualTo(setOf("A"))
    }
}

private fun childHavingSideValues(
        defaultSideValues: Set<String>,
        sideValues: Set<String> = setOf(),
        state: String = ENABLED.name) =
        object : ChildIntrinsicDetails {
            override val naturalId = "a"
            override val parentNaturalId = "b"
            override val state = state
            override val at = atZero
            override val value = null
            override val sideValues = sideValues
            override val defaultSideValues = defaultSideValues
            override val version = 1
        }

private fun parentHavingSideValues(
        children: Set<ChildIntrinsicDetails>,
        sideValues: Set<String> = setOf()) =
        object : TestParent {
            override val naturalId = "a"
            override val otherNaturalId = null
            override val state = ENABLED.name
            override val value = null
            override val sideValues = sideValues
            override val version = 1
            override val children = children
            override val at = null
        }

interface TestParent
    : ParentIntrinsicDetails,
        ParentComputedDetails
