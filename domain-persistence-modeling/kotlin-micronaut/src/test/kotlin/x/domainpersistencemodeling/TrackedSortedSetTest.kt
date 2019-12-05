package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test

private val doNothing: (String, MutableSet<String>) -> Unit = { _, _ -> }

internal class TrackedSortedSetTest {
    @Test
    fun `should notify when deleting through iterator`() {
        var removed = false
        val tracked = TrackedManyToOne(setOf("ABC"), doNothing) { _, _ ->
            removed = true
        }

        tracked.clear()

        expect(tracked).isEmpty()
        expect(removed).toBe(true)
    }

    @Test
    fun `should complain on misuse for optional-one`() {
        val tracked = TrackedOptionalOne("ABC", doNothing, doNothing)

        expect {
            tracked.add("BOB")
        }.toThrow<DomainException> { }
    }

    @Test
    fun `should complain on misuse for many-to-one`() {
        val tracked = TrackedManyToOne(setOf("BOB"), doNothing, doNothing)

        expect {
            tracked.add("BOB")
        }.toThrow<DomainException> { }

        expect {
            tracked.remove("SALLY")
        }.toThrow<DomainException> { }
    }
}
