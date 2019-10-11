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
        fun currentPersistedSubchildren() =
                children.findExisting(naturalId)!!.subchildren

        var saved = newUnsavedChild().update {
            subchildren.addAll(listOf("MOAT", "BAT"))
        }.save()

        expect(saved.version).toBe(1)
        expect(currentPersistedVersion()).toBe(1)
        expect(saved.subchildren).containsExactly("BAT", "MOAT")
        expect(currentPersistedSubchildren()).containsExactly("BAT", "MOAT")

        saved = saved.save() // No change

        expect(saved.version).toBe(1)
        expect(currentPersistedVersion()).toBe(1)

        saved = saved.update {
            subchildren.add("COW")
        }.save()

        expect(saved.version).toBe(2)
        expect(currentPersistedVersion()).toBe(2)
        expect(saved.subchildren).containsExactly("BAT", "COW", "MOAT")
        expect(currentPersistedSubchildren())
                .containsExactly("BAT", "COW", "MOAT")

        saved = saved.update {
            subchildren.clear()
            subchildren.add("NANCY")
        }.save()

        expect(saved.version).toBe(3)
        expect(currentPersistedVersion()).toBe(3)
        expect(saved.subchildren).containsExactly("NANCY")
        expect(currentPersistedSubchildren()).containsExactly("NANCY")
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
        val parent = newSavedParent()
        val unsaved = newUnsavedChild()
        val saved = unsaved.update {
            assignTo(parent)
        }.save()

        val found = children.findExisting(saved.naturalId)!!

        expect(found.parentNaturalId).toBe(parentNaturalId)

        val resaved = saved.update {
            unassignFromAny()
        }.save()

        val refound = children.findExisting(resaved.naturalId)!!

        expect(refound.parentNaturalId).toBe(null)
    }

    @Test
    fun shouldIncrementParentVersionWhenChildrenChange() {
        fun currentPersistedParentVersion(): Int {
            val found = parents.findExisting(parentNaturalId)
            println("PARENT found = $found")
            return found!!.version
        }

        val parent = newSavedParent()
        expect(parent.version).toBe(1)
        expect(currentPersistedParentVersion()).toBe(1)

        val child = newUnsavedChild().update {
            assignTo(parent)
        }.save()

        expect(child.parentNaturalId).toBe(parentNaturalId)
        expect(child.version).toBe(1)
        expect(currentPersistedVersion()).toBe(1)
        // TODO: This needs to be handled at domain level in parent
        // At the level of the child, in-memory parent still version 1
        expect(currentPersistedParentVersion()).toBe(2)

        child.update {
            value = "Elephant"
        }.save()

        expect(child.parentNaturalId).toBe(parentNaturalId)
        expect(child.version).toBe(2)
        expect(currentPersistedVersion()).toBe(2)
        expect(currentPersistedParentVersion()).toBe(3)

        child.update {
            unassignFromAny()
        }.save()

        expect(child.parentNaturalId).toBe(null)
        expect(child.version).toBe(3)
        expect(currentPersistedVersion()).toBe(3)
        expect(currentPersistedParentVersion()).toBe(4)
    }

    private fun newUnsavedChild() =
            children.findExistingOrCreateNew(naturalId)

    private fun newSavedParent() =
            parents.findExistingOrCreateNew(parentNaturalId).save()

    private fun currentPersistedVersion() =
            children.findExisting(naturalId)!!.version
}
