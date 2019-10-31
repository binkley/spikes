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
internal open class PersistedParentTest @Autowired constructor(
        private val parents: ParentFactory,
        private val children: ChildFactory,
        private val testListener: TestListener<ParentChangedEvent>) {
    companion object {
        private const val parentNaturalId = "a"
        private const val childNaturalId = "p"
    }

    @Test
    fun shouldCreateNew() {
        val found = parents.findExistingOrCreateNew(parentNaturalId)

        expect(found).toBe(parents.createNew(parentNaturalId))
        expect(found.children).isEmpty()
    }

    @Test
    fun shouldFindExisting() {
        val saved = newSavedParent()

        val found = parents.findExistingOrCreateNew(parentNaturalId)

        expect(found).toBe(saved)
        expect(found.children).isEmpty()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = parents.createNew(parentNaturalId)

        expect(unsaved.version).toBe(0)
        testListener.expectNext.isEmpty()

        val saved = unsaved.save()

        expect(parents.all().toList()).hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        testListener.expectNext.containsExactly(ParentChangedEvent(
                null,
                ParentResource(parentNaturalId, null, 1)))

        expect(currentPersistedParent()).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChanges() {
        val original = newSavedParent()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))
        testListener.expectNext.isEmpty()
    }

    @Test
    fun shouldMutate() {
        val original = newSavedParent()

        expect(original.changed).toBe(false)

        val value = "FOOBAR"
        val modified = original.update {
            this.value = value
        }

        expect(modified).toBe(original)
        expect(original.changed).toBe(true)
        expect(original.value).toBe(value)
        testListener.expectNext.isEmpty()

        original.save()

        expect(original.changed).toBe(false)
        testListener.expectNext.containsExactly(ParentChangedEvent(
                ParentResource(parentNaturalId, null, 1),
                ParentResource(parentNaturalId, value, 2)))
    }

    @Test
    fun shouldMutateChildren() {
        val parent = newSavedParent()
        val child = newSavedChild()

        parent.update {
            assign(child)
        }.save()
        testListener.reset()

        val value = "FOOBAR"
        // Silly example :)
        parent.update {
            children.forEach {
                it.update {
                    this.value = value
                }
            }
        }.save()

        expect(currentPersistedChild().value).toBe(value)

        testListener.expectNext.containsExactly(
                ChildChangedEvent(
                        ChildResource(childNaturalId, parentNaturalId,
                                null, emptySet(), 2),
                        ChildResource(childNaturalId, parentNaturalId,
                                value, emptySet(), 3)),
                ParentChangedEvent(
                        ParentResource(parentNaturalId, null, 2),
                        ParentResource(parentNaturalId, null, 3)))
    }

    @Test
    fun shouldDelete() {
        val existing = newSavedParent()

        existing.delete()

        expect(parents.all().toList()).isEmpty()
        expect {
            existing.version
        }.toThrow<DomainException> { }
        testListener.expectNext.containsExactly(ParentChangedEvent(
                ParentResource(parentNaturalId, null, 1),
                null))
    }

    @Test
    fun shouldNotDelete() {
        val parent = newSavedParent()
        val child = newSavedChild()

        parent.update {
            assign(child)
        }

        expect {
            parent.delete()
        }.toThrow<DomainException> { }
    }

    @Test
    fun shouldNotAssignAlreadyAssignedChild() {
        val parent = newSavedParent()
        val child = newSavedChild()

        parent.update {
            assign(child)
        }.save()

        expect {
            parent.update {
                assign(child)
            }.save()
        }.toThrow<DomainException> { }
    }

    @Test
    fun shouldAssignAndUnassignChild() {
        val parent = newSavedParent()
        val child = newSavedChild()

        expect(parent.children).isEmpty()

        val parentAssignedWithChild = parent.update {
            assign(child)
        }.save().domain

        expect(parent.children).containsExactly(child)
        expect(parentAssignedWithChild.version).toBe(2)
        expect(currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        testListener.expectNext.containsExactly(
                ChildChangedEvent(
                        ChildResource(childNaturalId, null, null,
                                emptySet(), 1),
                        ChildResource(childNaturalId, parentNaturalId, null,
                                emptySet(), 2)),
                ParentChangedEvent(
                        ParentResource(parentNaturalId, null, 1),
                        ParentResource(parentNaturalId, null, 2)))

        val childUnassigned = parent.update {
            unassign(child)
        }.save().domain

        expect(parent.children).isEmpty()
        expect(childUnassigned.version).toBe(3)
        expect(currentPersistedChild().parentNaturalId).toBe(null)
        testListener.expectNext.containsExactly(
                ChildChangedEvent(
                        ChildResource(childNaturalId, parentNaturalId, null,
                                emptySet(), 2),
                        ChildResource(childNaturalId, null, null,
                                emptySet(), 3)),
                ParentChangedEvent(
                        ParentResource(parentNaturalId, null, 2),
                        ParentResource(parentNaturalId, null, 3)))
    }

    private fun newSavedChild(): Child {
        val saved = children.createNew(childNaturalId).save()
        expect(saved.changed).toBe(true)
        val child = saved.domain
        testListener.reset()
        return child
    }

    private fun currentPersistedChild() =
            children.findExisting(childNaturalId)!!

    private fun newSavedParent(): Parent {
        val saved = parents.createNew(parentNaturalId).save()
        expect(saved.changed).toBe(true)
        val parent = saved.domain
        testListener.reset()
        return parent
    }

    private fun currentPersistedParent() =
            parents.findExisting(parentNaturalId)!!
}
