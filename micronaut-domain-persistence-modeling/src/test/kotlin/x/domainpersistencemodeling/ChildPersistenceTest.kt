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
class ChildPersistenceTest {
    @Inject
    lateinit var children: ChildFactory
    @Inject
    lateinit var parents: ParentFactory
    @Inject
    lateinit var testListener: TestListener<ChildChangedEvent>

    companion object {
        const val naturalId = "p"
    }

    @AfterEach
    fun tearDown() {
        testListener.reset()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = newUnsavedChild()
        val saved = unsaved.save()

        val found = children.findExisting(saved.naturalId)

        expect(found).toBe(saved)

        testListener.expectNext.containsExactly(
                ChildChangedEvent(null, ChildResource(
                        naturalId, null, null, sortedSetOf(), 1)))
    }

    @Test
    fun shouldRoundTripSubchildren() {
        val unsaved = newUnsavedChild()
        unsaved.update {
            subchildren.addAll(listOf("MOAT", "BAT"))
        }
        val saved = unsaved.save()

        val found = children.findExisting(saved.naturalId)!!

        expect(found.subchildren).containsExactly("BAT", "MOAT")

        found.update {
            subchildren.clear()
            subchildren.add("NANCY")
        }
        val resaved = found.save()

        val refound = children.findExisting(resaved.naturalId)!!

        expect(refound.subchildren).containsExactly("NANCY")
    }

    @Test
    fun shouldDelete() {
        val unsaved = newUnsavedChild()
        val saved = unsaved.save()

        saved.delete()

        val found = children.findExisting(naturalId)

        expect(found).toBe(null)

        expect {
            saved.naturalId
        }.toThrow<NullPointerException> { }

        testListener.expectNext.containsExactly(
                ChildChangedEvent(null, ChildResource(
                        naturalId, null, null, sortedSetOf(), 1)),
                ChildChangedEvent(ChildResource(
                        naturalId, null, null, sortedSetOf(), 1), null))
    }

    @Test
    fun shouldAddAndRemoveToFromParents() {
        val parent = parents.findExistingOrCreateNew("a").save()

        val unsaved = newUnsavedChild()
        val saved = unsaved.update {
            assignTo(parent)
        }.save()

        val found = children.findExisting(saved.naturalId)!!

        expect(found.parentNaturalId).toBe(parent.naturalId)

        val resaved = saved.update {
            unassignFromAny()
        }.save()

        val refound = children.findExisting(resaved.naturalId)!!

        expect(refound.parentNaturalId).toBe(null)
    }

    @Test
    fun shouldIncrementParentVersionWhenChildrenChange() {
        val parent = parents.findExistingOrCreateNew("a").save()

        expect(parent.version).toBe(1)

        val child = newUnsavedChild().update {
            assignTo(parent)
        }.save()

        expect(child.version).toBe(1)
        expect(parent.version).toBe(2)

        child.update {
            value = "Elephant"
        }.save()

        expect(child.version).toBe(2)
        expect(parent.version).toBe(3)

        child.update {
            unassignFromAny()
        }.save()

        expect(child.version).toBe(3)
        expect(parent.version).toBe(4)
    }

    private fun newUnsavedChild() =
            children.findExistingOrCreateNew(naturalId)
}
