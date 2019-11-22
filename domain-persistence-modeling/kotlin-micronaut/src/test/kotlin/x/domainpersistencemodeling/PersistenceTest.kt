package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.child.ChildRecord
import x.domainpersistencemodeling.child.ChildRepository
import x.domainpersistencemodeling.child.upsert
import x.domainpersistencemodeling.parent.ParentRecord
import x.domainpersistencemodeling.parent.ParentRepository
import x.domainpersistencemodeling.parent.upsert
import javax.inject.Inject

@MicronautTest
internal open class PersistenceTest {
    companion object {
        const val parentNaturalId = "a"
        const val childNaturalId = "p"

        fun newUnsavedParent() =
                ParentRecord(
                        parentNaturalId)

        fun newUnsavedChild() =
                ChildRecord(
                        childNaturalId)
    }

    @Inject
    private lateinit var parents: ParentRepository
    @Inject
    private lateinit var children: ChildRepository

    @Test
    fun shouldRoundTripParent() {
        val unsaved = newUnsavedParent()

        expect(unsaved.version).toBe(0)

        val saved = parents.upsert(unsaved)

        expect(unsaved.version).toBe(1)
        expect(saved).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChangesInParent() {
        val saved = newSavedParent()

        expect(saved.version).toBe(1)

        val resaved = parents.upsert(saved)

        expect(resaved).toBe(null)
        expect(saved.version).toBe(1)
    }

    @Test
    fun shouldRoundTripChild() {
        val unsaved = newUnsavedChild()

        expect(unsaved.version).toBe(0)

        val saved = children.upsert(unsaved)

        expect(unsaved.version).toBe(1)
        expect(saved).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChangesInChild() {
        val saved = newSavedChild()

        expect(saved.version).toBe(1)

        val resaved = children.upsert(saved)

        expect(resaved).toBe(null)
        expect(saved.version).toBe(1)
    }

    @Test
    fun shouldAssignChildWhenCreating() {
        newSavedParent()

        val savedChild = children.upsert(newUnsavedChild().apply {
            this.parentNaturalId = PersistenceTest.parentNaturalId
        })!!

        expect(savedChild.version).toBe(1)
        expect(findExistingParent().version).toBe(2)
        expect(children.findByParentNaturalId(parentNaturalId))
                .containsExactly(savedChild)
    }

    @Test
    fun shouldAssignChildWhenModifying() {
        newSavedParent()

        val savedChild = newSavedChild()

        expect(savedChild.version).toBe(1)

        savedChild.parentNaturalId = parentNaturalId
        val updatedChild = children.upsert(savedChild)!!

        expect(updatedChild.version).toBe(2)
        expect(findExistingParent().version).toBe(2)
        expect(children.findByParentNaturalId(parentNaturalId))
                .containsExactly(savedChild)
    }

    private fun newSavedParent(): ParentRecord {
        return parents.upsert(newUnsavedParent())!!
    }

    private fun newSavedChild(): ChildRecord {
        return children.upsert(newUnsavedChild())!!
    }

    private fun findExistingParent(): ParentRecord {
        return parents.findByNaturalId(parentNaturalId).orElseThrow()
    }
}
