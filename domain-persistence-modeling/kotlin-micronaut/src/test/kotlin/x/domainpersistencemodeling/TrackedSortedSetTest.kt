package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test

internal class TrackedSortedSetTest {
    @Test
    fun `should notify when deleting through iterator`() {
        var removed = false
        val tracked = TrackedManyToOne(setOf("ABC"), { _, _ -> }, { _, _ ->
            removed = true
        })

        tracked.clear()

        expect(tracked).isEmpty()
        expect(removed).toBe(true)
    }

    @Test
    fun `should complain on misuse`() {
        val tracked = TrackedOptionalOne(setOf("ABC"),
            { _, _ -> }, { _, _ -> })

        expect {
            tracked.add("BOB")
        }.toThrow<IllegalStateException> { }
    }
}
