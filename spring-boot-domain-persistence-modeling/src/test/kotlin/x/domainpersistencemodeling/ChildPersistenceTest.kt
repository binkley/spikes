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
@Import(value = [
    PersistedChildFactory::class,
    PersistedParentFactory::class,
    TestListener::class])
class ChildPersistenceTest @Autowired constructor(
        private val children: ChildFactory,
        private val parents: ParentFactory,
        private val testListener: TestListener<ChildChangedEvent>) {
    companion object {
        const val naturalId = "p"
        const val parentNaturalId = "a"
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
        var saved = unsaved.save()

        var found = children.findExisting(saved.naturalId)!!
        expect(found.subchildren).containsExactly("BAT", "MOAT")

        found.update {
            subchildren.add("COW")
        }
        saved = found.save()

        found = children.findExisting(saved.naturalId)!!
        expect(found.subchildren).containsExactly("BAT", "COW", "MOAT")

        found.update {
            subchildren.clear()
            subchildren.add("NANCY")
        }
        saved = found.save()

        found = children.findExisting(saved.naturalId)!!
        expect(found.subchildren).containsExactly("NANCY")
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
        fun currentParentVersion(): Int {
            val found = parents.findExisting(parentNaturalId)
            println("PARENT found = ${found}")
            return found!!.version
        }

        val parent = parents.findExistingOrCreateNew(parentNaturalId).save()
        expect(currentParentVersion()).toBe(1)

        val child = newUnsavedChild().update {
            assignTo(parent)
        }.save()

        expect(child.version).toBe(1)
        // TODO: Why is parent still version 1?
        expect(currentParentVersion()).toBe(2)

        child.update {
            value = "Elephant"
        }.save()

        expect(child.version).toBe(2)
        expect(currentParentVersion()).toBe(3)

        child.update {
            unassignFromAny()
        }.save()

        expect(child.version).toBe(3)
        expect(currentParentVersion()).toBe(4)
    }

    private fun newUnsavedChild() =
            children.findExistingOrCreateNew(naturalId)
}
