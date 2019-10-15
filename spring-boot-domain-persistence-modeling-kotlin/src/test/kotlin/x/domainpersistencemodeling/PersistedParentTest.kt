package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.context.SpringBootTest

@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest
internal class PersistedParentTest @Autowired constructor(
        private val parents: ParentFactory,
        private val children: ChildFactory,
        private val testListener: TestListener<ParentChangedEvent>) {
    companion object {
        private const val parentNaturalId = "a"
        private const val childNaturalId = "p"
    }

    @AfterEach
    fun tearDown() {
        testListener.reset()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = newUnsavedParent()
        val saved = unsaved.save()

        val found = parents.findExisting(parentNaturalId)

        expect(found).toBe(saved)

        testListener.expectNext.containsExactly(
                ParentChangedEvent(null,
                        ParentResource(parentNaturalId, null, 1)))
    }

    @Test
    fun shouldDelete() {
        val unsaved = newUnsavedParent()
        val saved = unsaved.save()

        saved.delete()

        val found = parents.findExisting(parentNaturalId)

        expect(found).toBe(null)

        expect {
            saved.naturalId
        }.toThrow<NullPointerException> { }

        testListener.expectNext.containsExactly(
                ParentChangedEvent(null,
                        ParentResource(parentNaturalId, null, 1)),
                ParentChangedEvent(ParentResource(parentNaturalId, null, 1),
                        null))
    }

    private fun newUnsavedParent(): Parent {
        return parents.findExistingOrCreateNew(parentNaturalId)
    }

    private fun newSavedChild(): Child {
        val saved = children.createNew(parentNaturalId).save();
        expect(saved.changed).toBe(true)
        val child = saved.domain;
        testListener.reset();
        return child;
    }

    private fun currentPersistedChild() =
            children.findExisting(parentNaturalId)!!

    private fun newSavedParent(): Parent {
        val saved = parents.createNew(parentNaturalId).save();
        expect(saved.changed).toBe(true)
        val parent = saved.domain;
        testListener.reset();
        return parent;
    }

    private fun currentPersistedParent() =
            parents.findExisting(parentNaturalId)!!
}
