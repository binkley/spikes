package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.hasSize
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult

@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest
@Transactional
internal open class PersistedChildTest @Autowired constructor(
        private val children: ChildFactory,
        private val parents: ParentFactory,
        private val testListener: TestListener<ChildChangedEvent>) {
    companion object {
        const val parentNaturalId = "a"
        const val childNaturalId = "p"
    }

    @Test
    fun shouldCreateNew() {
        val found = children.findExistingOrCreateNew(childNaturalId)

        expect(found).toBe(children.createNew(childNaturalId))
        expect(found.existing).toBe(false)
    }

    @Test
    fun shouldFindExisting() {
        val saved = newSavedChild()

        val found = children.findExistingOrCreateNew(childNaturalId)

        expect(found).toBe(saved)
        expect(found.existing).toBe(true)
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = children.createNew(childNaturalId)

        expect(unsaved.version).toBe(0)
        testListener.expectNext.isEmpty()

        val saved = unsaved.save()

        expect(children.all().toList()).hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        testListener.expectNext.containsExactly(ChildChangedEvent(
                null,
                ChildResource(childNaturalId, null, null,
                        emptySet(), 1)))

        expect(currentPersistedChild()).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChanges() {
        val original = newSavedChild()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))
        testListener.expectNext.isEmpty()
    }

    @Test
    fun shouldMutate() {
        val original = newSavedChild()

        expect(original.changed).toBe(false)

        val value = "FOOBAR"
        original.update {
            this.value = value
        }

        expect(original.changed).toBe(true)
        expect(original.value).toBe(value)
        testListener.expectNext.isEmpty()

        original.save()

        expect(original.changed).toBe(false)
        testListener.expectNext.containsExactly(ChildChangedEvent(
                ChildResource(childNaturalId, null, null,
                        emptySet(), 1),
                ChildResource(childNaturalId, null, value,
                        emptySet(), 2)))
    }

    @Test
    fun shouldDelete() {
        val existing = newSavedChild()

        existing.delete()

        expect(children.all().toList()).isEmpty()
        expect {
            existing.version
        }.toThrow<DomainException> { }
        testListener.expectNext.containsExactly(ChildChangedEvent(
                ChildResource(childNaturalId, null, null,
                        emptySet(), 1),
                null))
    }

    @Test
    fun shouldAssignChildAtCreation() {
        val parent = newSavedParent()

        expect(parent.version).toBe(1)

        val unsaved = children.createNew(childNaturalId)
        unsaved.update {
            assignTo(parent)
        }

        expect(unsaved.parentNaturalId).toBe(parentNaturalId)

        unsaved.save()

        expect(currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        expect(currentPersistedParent().version).toBe(2)
        testListener.expectNext.containsExactly(ChildChangedEvent(
                null,
                ChildResource(childNaturalId, parentNaturalId, null,
                        emptySet(), 1)))
    }

    @Test
    fun shouldAssignChildAtMutation() {
        val parent = newSavedParent()
        val child = newSavedChild()

        expect(parent.version).toBe(1)

        val assigned = child
        assigned.update {
            assignTo(parent)
        }

        expect(assigned.parentNaturalId).toBe(parentNaturalId)
        testListener.expectNext.isEmpty()

        assigned.save()

        expect(assigned.version).toBe(2)
        expect(currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        expect(currentPersistedParent().version).toBe(2)
        testListener.expectNext.containsExactly(ChildChangedEvent(
                ChildResource(childNaturalId, null, null,
                        emptySet(), 1),
                ChildResource(childNaturalId, parentNaturalId, null,
                        emptySet(), 2)))
    }

    @Test
    fun shouldUnassignChild() {
        val parent = newSavedParent()
        val child = children.createNew(childNaturalId)
        child.update {
            assignTo(parent)
        }
        child.save().domain
        testListener.reset()

        expect(parent.version).toBe(1)

        child.update(MutableChild::unassignFromAny)
        child.save()

        expect(child.version).toBe(2)
        expect(currentPersistedChild().parentNaturalId).toBe(null)
        // Created, assigned by child, unassigned by child == version 3
        expect(currentPersistedParent().version).toBe(3)
        testListener.expectNext.containsExactly(ChildChangedEvent(
                ChildResource(childNaturalId, parentNaturalId, null,
                        emptySet(), 1),
                ChildResource(childNaturalId, null, null,
                        emptySet(), 2)))
    }

    private fun newSavedChild(): Child {
        val saved = children.createNew(childNaturalId).save()
        expect(saved.changed).toBe(true)
        val child = saved.domain
        testListener.reset()
        return child
    }

    private fun currentPersistedChild(): Child {
        return children.findExisting(childNaturalId)!!
    }

    private fun newSavedParent(): Parent {
        val saved = parents.createNew(parentNaturalId).save()
        expect(saved.changed).toBe(true)
        val parent = saved.domain
        testListener.reset()
        return parent
    }

    private fun currentPersistedParent(): Parent {
        return parents.findExisting(parentNaturalId)!!
    }
}
