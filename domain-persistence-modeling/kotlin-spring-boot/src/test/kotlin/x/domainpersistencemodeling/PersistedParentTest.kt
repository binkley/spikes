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
                ParentSnapshot(parentNaturalId, null, setOf(), 1)))

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
        original.update {
            this.value = value
        }

        expect(original.changed).toBe(true)
        expect(original.value).toBe(value)
        testListener.expectNext.isEmpty()

        original.save()

        expect(original.changed).toBe(false)
        testListener.expectNext.containsExactly(ParentChangedEvent(
                ParentSnapshot(parentNaturalId, null, setOf(), 1),
                ParentSnapshot(parentNaturalId, value, setOf(), 2)))
    }

    @Test
    fun shouldMutateChildren() {
        val parent = newSavedParent()
        val child = newSavedUnassignedChild()

        parent.assign(child)

        val value = "FOOBAR"
        parent.update {
            children.forEach {
                it.update {
                    this.value = value
                }
            }
        }
        parent.save()

        expect(currentPersistedChild().value).toBe(value)

        testListener.expectNext.containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, null,
                                null, emptySet(), 1),
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                value, emptySet(), 2)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, null, setOf(), 1),
                        ParentSnapshot(parentNaturalId, null, setOf(), 2)))
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
                ParentSnapshot(parentNaturalId, null, setOf(), 1),
                null))
    }

    @Test
    fun shouldNotDelete() {
        val parent = newSavedParent()
        val child = newSavedUnassignedChild()

        parent.assign(child)

        expect {
            parent.delete()
        }.toThrow<DomainException> { }
    }

    @Test
    fun shouldNotAssignAlreadyAssignedChild() {
        val parent = newSavedParent()
        val child = newSavedUnassignedChild()

        parent.assign(child)

        expect {
            parent.assign(child)
        }.toThrow<DomainException> { }
    }

    @Test
    fun shouldAssignAndUnassignChild() {
        val parent = newSavedParent()
        val child = newSavedUnassignedChild()

        expect(parent.children).isEmpty()

        val assignedChild = parent.assign(child)
        val parentAssignedWithChild = parent.save().domain

        expect(parent.children).containsExactly(child)
        expect(parentAssignedWithChild.version).toBe(2)
        expect(currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        testListener.expectNext.containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, null, null,
                                emptySet(), 1),
                        ChildSnapshot(childNaturalId, parentNaturalId, null,
                                emptySet(), 2)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, null, setOf(), 1),
                        ParentSnapshot(parentNaturalId, null, setOf(), 2)))

        parent.unassign(assignedChild)
        val childUnassigned = parent.save().domain

        expect(parent.children).isEmpty()
        expect(childUnassigned.version).toBe(3)
        expect(currentPersistedChild().parentNaturalId).toBe(null)
        testListener.expectNext.containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, parentNaturalId, null,
                                emptySet(), 2),
                        ChildSnapshot(childNaturalId, null, null,
                                emptySet(), 3)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, null, setOf(), 2),
                        ParentSnapshot(parentNaturalId, null, setOf(), 3)))
    }

    private fun newSavedUnassignedChild(): UnassignedChild {
        val saved = children.createNew(childNaturalId).save()
        expect(saved.changed).toBe(true)
        val child = saved.domain
        testListener.reset()
        return child as UnassignedChild
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
