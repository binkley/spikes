package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.child.AssignedChild
import x.domainpersistencemodeling.child.ChildSimpleDetails
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.parent.ParentDependentDetails
import x.domainpersistencemodeling.parent.ParentSimpleDetails
import java.time.OffsetDateTime

internal class AtTest {
    @Test
    internal fun `should have no "at" without children`() {
        val children: Set<AssignedChild> = setOf()

        expect(children.at).toBe(null)
    }

    @Test
    internal fun `should have an "at" without children`() {
        val parent = parentHavingAt(atZero)

        expect(computeDue(parent, parent.children)).toBe(atZero)
    }

    @Test
    internal fun `should have a minimal "at" with children`() {
        val childDetailsA = childHavingAt(atZero)
        val childDetailsB = childHavingAt(atZero.plusDays(1L))

        val children: Set<ChildSimpleDetails> =
            setOf(childDetailsA, childDetailsB)

        expect(children.at).toBe(childDetailsA.at)
    }

    @Test
    internal fun `should default to children "at"`() {
        val due = atZero
        val parent = parentHavingAt(
            null,
            childHavingAt(due)
        )

        expect(computeDue(parent, parent.children)).toBe(due)
    }

    @Test
    internal fun `should override children "at"`() {
        val due = atZero
        val parent = parentHavingAt(
            due,
            childHavingAt(due.plusDays(1L))
        )

        expect(computeDue(parent, parent.children)).toBe(due)
    }

    @Test
    internal fun `should complain when parent "at" after children "at"`() {
        val parent = parentHavingAt(
            atZero.plusDays(1L),
            childHavingAt(atZero)
        )

        expect {
            computeDue(parent, parent.children)
        }.toThrow<DomainException> { }
    }
}

private fun childHavingAt(at: OffsetDateTime) =
    object : ChildSimpleDetails {
        override val naturalId = "a"
        override val parentNaturalId = "b"
        override val state = "IRRELEVANT"
        override val at = at
        override val value: String? = null
        override val defaultSideValues = setOf<String>()
        override val sideValues = setOf<String>()
        override val version = 1
    }

private fun parentHavingAt(
    at: OffsetDateTime?,
    vararg children: ChildSimpleDetails
) =
    object : TestParentForAt {
        override val naturalId = "a"
        override val state = ENABLED.name
        override val value: String? = null
        override val sideValues = setOf<String>()
        override val version = 1
        override val other = null as Other?
        override val children = children.toSet()
        override val due: OffsetDateTime? = null
        override val at: OffsetDateTime? = at
    }

private interface TestParentForAt
    : ParentSimpleDetails,
    ParentDependentDetails
