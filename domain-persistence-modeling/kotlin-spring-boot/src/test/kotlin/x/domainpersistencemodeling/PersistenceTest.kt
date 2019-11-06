package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.transaction.annotation.Transactional

/** @todo Update to use [LiveTestBase], or delete and cover elsewhere */
@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@Transactional
internal open class PersistenceTest @Autowired constructor(
        private val parents: ParentRepository,
        private val children: ChildRepository) {
    companion object {
        const val parentNaturalId = "a"
        const val childNaturalId = "p"

        fun newUnsavedParent() = ParentRecord(parentNaturalId)
        fun newUnsavedChild() = ChildRecord(childNaturalId)
    }

    @Test
    fun shouldRoundTripParent() {
        val unsaved = newUnsavedParent()

        expect(unsaved.version).toBe(0)

        val saved = parents.upsert(unsaved).orElseThrow()

        expect(unsaved.version).toBe(1)
        expect(saved).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChangesInParent() {
        val saved = newSavedParent()

        expect(saved.version).toBe(1)

        val resaved = parents.upsert(saved)

        expect(resaved.isEmpty).toBe(true)
        expect(saved.version).toBe(1)
    }

    @Test
    fun shouldRoundTripChild() {
        val unsaved = newUnsavedChild()

        expect(unsaved.version).toBe(0)

        val saved = children.upsert(unsaved).orElseThrow()

        expect(unsaved.version).toBe(1)
        expect(saved).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChangesInChild() {
        val saved = newSavedChild()

        expect(saved.version).toBe(1)

        val resaved = children.upsert(saved)

        expect(resaved.isEmpty).toBe(true)
        expect(saved.version).toBe(1)
    }

    @Test
    fun shouldAssignChildWhenCreating() {
        newSavedParent()

        val savedChild = children.upsert(newUnsavedChild().apply {
            this.parentNaturalId = PersistenceTest.parentNaturalId
        }).orElseThrow()

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
        val updatedChild = children.upsert(savedChild).orElseThrow()

        expect(updatedChild.version).toBe(2)
        expect(findExistingParent().version).toBe(2)
        expect(children.findByParentNaturalId(parentNaturalId))
                .containsExactly(savedChild)
    }

    private fun newSavedParent(): ParentRecord {
        return parents.upsert(newUnsavedParent()).orElseThrow()
    }

    private fun newSavedChild(): ChildRecord {
        return children.upsert(newUnsavedChild()).orElseThrow()
    }

    private fun findExistingParent(): ParentRecord {
        return parents.findByNaturalId(parentNaturalId).orElseThrow()
    }
}
