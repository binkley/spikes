package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class ParentPersistenceTest {
    @Inject
    lateinit var parents: ParentFactory
    @Inject
    lateinit var testListener: TestListener<ParentChangedEvent>

    companion object {
        private const val naturalId = "a"
    }

    @AfterEach
    fun tearDown() {
        testListener.reset()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = newUnsavedParent()
        val saved = unsaved.save()

        val found = parents.findExisting(naturalId)

        expect(found).toBe(saved)

        testListener.expectNext.containsExactly(
                ParentChangedEvent(null, ParentResource(naturalId, null, 1)))
    }

    @Test
    fun shouldDelete() {
        val unsaved = newUnsavedParent()
        val saved = unsaved.save()

        saved.delete()

        val found = parents.findExisting(naturalId)

        expect(found).toBe(null)

        expect {
            saved.naturalId
        }.toThrow<NullPointerException> { }

        testListener.expectNext.containsExactly(
                ParentChangedEvent(null, ParentResource(naturalId, null, 1)),
                ParentChangedEvent(ParentResource(naturalId, null, 1), null))
    }

    private fun newUnsavedParent() =
            parents.findExistingOrCreateNew(naturalId)
}
