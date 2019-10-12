package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.context.annotation.Import

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@Import(value = [PersistedParentFactory::class, TestListener::class])
internal class ParentPersistenceTest @Autowired constructor(
        private val parents: ParentFactory,
        private val testListener: TestListener<ParentChangedEvent>) {
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