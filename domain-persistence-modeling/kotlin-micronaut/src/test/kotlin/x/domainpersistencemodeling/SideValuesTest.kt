package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.KnownState.DISABLED
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.child.ChildSimpleDetails
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.parent.ParentDependentDetails
import x.domainpersistencemodeling.parent.ParentSimpleDetails
import java.time.OffsetDateTime

internal class SideValuesTest {
    @Test
    internal fun `should use defaults for children`() {
        val child = childHavingSideValues(
            defaultSideValues = setOf("A")
        )

        expect(child.currentSideValues).containsExactly("A")
    }

    @Test
    internal fun `should ignore defaults for disabled children`() {
        val child = childHavingSideValues(
            defaultSideValues = setOf("A"),
            sideValues = setOf(),
            state = DISABLED.name
        )

        expect(child.currentSideValues).isEmpty()
    }

    @Test
    internal fun `should use defaults for children in an unknown state`() {
        val child = childHavingSideValues(
            defaultSideValues = setOf("A"),
            sideValues = setOf(),
            state = "FUNKY"
        )

        expect(child.currentSideValues).containsExactly("A")
    }

    @Test
    internal fun `should use overrides for children`() {
        val child = childHavingSideValues(
            defaultSideValues = setOf("A"),
            sideValues = setOf("B")
        )

        expect(child.currentSideValues).containsExactly("B")
    }

    @Test
    internal fun `should ignore overrides for disabled children`() {
        val child = childHavingSideValues(
            defaultSideValues = setOf("B"),
            sideValues = setOf("A"),
            state = DISABLED.name
        )

        expect(child.currentSideValues).isEmpty()
    }

    @Test
    internal fun `should use children's intersection for parents`() {
        val parent = parentHavingSideValues(
            childHavingSideValues(setOf("A", "B")),
            childHavingSideValues(setOf("A", "C")),
            childHavingSideValues(setOf())
        )

        expect(parent.currentSideValues).containsExactly("A")
    }

    @Test
    internal fun `should use overrides for parents`() {
        val parent = parentHavingSideValues(
            childHavingSideValues(setOf("B")),
            sideValues = setOf("A")
        )

        expect(parent.currentSideValues).containsExactly("A")
    }
}

private fun childHavingSideValues(
    defaultSideValues: Set<String>,
    sideValues: Set<String> = setOf(),
    state: String = ENABLED.name
) =
    object : ChildSimpleDetails {
        override val naturalId = "a"
        override val parentNaturalId = "b"
        override val state = state
        override val at = atZero
        override val value: String? = null
        override val defaultSideValues = defaultSideValues
        override val sideValues = sideValues
        override val version = 1
    }

private fun parentHavingSideValues(
    vararg children: ChildSimpleDetails,
    sideValues: Set<String> = setOf()
) =
    object : TestParentForSideValues {
        override val naturalId = "a"
        override val state = ENABLED.name
        override val value: String? = null
        override val sideValues = sideValues
        override val version = 1
        override val other = null as Other?
        override val children = children.toSet()
        override val due: OffsetDateTime? = null
        override val at: OffsetDateTime? = null
    }

private interface TestParentForSideValues
    : ParentSimpleDetails,
    ParentDependentDetails
