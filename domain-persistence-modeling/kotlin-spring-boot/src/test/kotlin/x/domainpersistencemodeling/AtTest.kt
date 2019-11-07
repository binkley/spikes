package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

internal class AtTest {
    @Test
    internal fun `should have no "at" without children`() {
        val children: Set<AssignedChild> = setOf()

        expect(children.at).toBe(null)
    }

    @Test
    internal fun `should have minimal "at" with children`() {
        val childDetailsA = childHavingAt(atZero)
        val childDetailsB = childHavingAt(atZero.plusNanos(1_000L))

        val children: Set<ChildIntrinsicDetails> =
                setOf(childDetailsA, childDetailsB)

        expect(children.at).toBe(childDetailsA.at)
    }
}

private fun childHavingAt(at: OffsetDateTime) =
        object : ChildIntrinsicDetails {
            override val naturalId = "a"
            override val parentNaturalId = "b"
            override val state = "IRRELEVANT"
            override val at = at
            override val value = null
            override val sideValues = setOf<String>()
            override val defaultSideValues = setOf<String>()
            override val version = 1
        }
