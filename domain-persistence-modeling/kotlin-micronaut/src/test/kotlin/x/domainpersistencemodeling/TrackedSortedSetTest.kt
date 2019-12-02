package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.TrackingArity.MANY
import x.domainpersistencemodeling.TrackingArity.OPTIONAL_ONE

internal class TrackedSortedSetTest {
    @Test
    fun `should notify when deleting through iterator`() {
        var removed = false
        val tracked =
            TrackedSortedSet(MANY,
                setOf("ABC"),
                { _, _ -> }, { _, _ ->
                    removed = true
                })

        tracked.clear()

        expect(tracked).isEmpty()
        expect(removed).toBe(true)
    }

    @Test
    fun `should complain on misuse`() {
        val tracked =
            TrackedSortedSet(
                OPTIONAL_ONE,
                setOf("ABC"),
                { _, _ -> },
                { _, _ -> })

        expect {
            tracked.add("BOB")
        }.toThrow<IllegalStateException> { }
    }
}
