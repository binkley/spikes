package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.KnownState.DISABLED
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.child.ChildSimpleDetails
import x.domainpersistencemodeling.parent.ParentDependentDetails
import x.domainpersistencemodeling.parent.ParentSimpleDetails
import java.time.OffsetDateTime

internal class SideValuesTest {
    @Test
    internal fun `should use defaults for children`() {
        val child = childHavingSideValues(setOf("A"))

        expect(child.currentSideValues).toBe(setOf("A"))
    }

    @Test
    internal fun `should ignore defaults for disabled children`() {
        val child = childHavingSideValues(
            setOf("A"), setOf(),
            DISABLED.name
        )

        expect(child.currentSideValues).isEmpty()
    }

    @Test
    internal fun `should use overrides for children`() {
        val child = childHavingSideValues(setOf("A"), setOf("B"))

        expect(child.currentSideValues).toBe(setOf("B"))
    }

    @Test
    internal fun `should ignore overrides for disabled children`() {
        val child = childHavingSideValues(
            setOf("B"), setOf("A"),
            DISABLED.name
        )

        expect(child.currentSideValues).isEmpty()
    }

    @Test
    internal fun `should use children's intersection for parents`() {
        val parent = parentHavingSideValues(
            setOf(
                childHavingSideValues(setOf("A", "B")),
                childHavingSideValues(setOf("A", "C")),
                childHavingSideValues(setOf())
            )
        )

        expect(parent.currentSideValues).toBe(setOf("A"))
    }

    @Test
    internal fun `should use overrides for parents`() {
        val parent = parentHavingSideValues(
            setOf(childHavingSideValues(setOf("B"))), setOf("A")
        )

        expect(parent.currentSideValues).toBe(setOf("A"))
    }
}

private fun childHavingSideValues(
    defaultSideValues: Set<String>,
    sideValues: Set<String> = setOf(),
    state: String = ENABLED.name
) =
    object : ChildSimpleDetails {
        override val naturalId = "a"
        override val otherNaturalId: String? = null
        override val parentNaturalId = "b"
        override val state = state
        override val at = atZero
        override val value: String? = null
        override val sideValues = sideValues
        override val defaultSideValues = defaultSideValues
        override val version = 1
    }

private fun parentHavingSideValues(
    children: Set<ChildSimpleDetails>,
    sideValues: Set<String> = setOf()
) =
    object : TestParent {
        override val naturalId = "a"
        override val otherNaturalId: String? = null
        override val state = ENABLED.name
        override val value: String? = null
        override val sideValues = sideValues
        override val version = 1
        override val children = children
        override val at: OffsetDateTime? = null
    }

interface TestParent
    : ParentSimpleDetails,
    ParentDependentDetails
