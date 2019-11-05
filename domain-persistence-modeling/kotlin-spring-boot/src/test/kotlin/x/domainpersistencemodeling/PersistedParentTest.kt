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
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult

@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest
@Transactional
internal open class PersistedParentTest @Autowired constructor(
        private val testing: Testing) {
    @Test
    fun shouldCreateNew() {
        val found = testing.findExistingOrCreateNewParent()

        expect(found).toBe(testing.createNewParent())
        expect(found.children).isEmpty()
    }

    @Test
    fun shouldFindExisting() {
        val saved = testing.newSavedParent()

        val found = testing.findExistingOrCreateNewParent()

        expect(found).toBe(saved)
        expect(found.children).isEmpty()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = testing.createNewParent()

        expect(unsaved.version).toBe(0)
        testing.expectDomainChangedEvents().isEmpty()

        val saved = unsaved.save()

        expect(testing.allParents().toList()).hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        testing.expectDomainChangedEvents().containsExactly(
                ParentChangedEvent(
                        null,
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 1)))

        expect(testing.currentPersistedParent()).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChanges() {
        val original = testing.newSavedParent()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))
        testing.expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldMutate() {
        val original = testing.newSavedParent()

        expect(original.changed).toBe(false)

        val value = "FOOBAR"
        original.update {
            this.value = value
        }

        expect(original.changed).toBe(true)
        expect(original.value).toBe(value)
        testing.expectDomainChangedEvents().isEmpty()

        original.save()

        expect(original.changed).toBe(false)
        testing.expectDomainChangedEvents()
                .containsExactly(ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 1),
                        ParentSnapshot(parentNaturalId, ENABLED.name, value,
                                setOf(), 2)))
    }

    @Test
    fun shouldMutateChildren() {
        val parent = testing.newSavedParent()
        val child = testing.newSavedUnassignedChild()

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

        expect(testing.currentPersistedChild().value).toBe(value)

        testing.expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, null, emptySet(), 1),
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, value, emptySet(), 2)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 1),
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 2)))
    }

    @Test
    fun shouldDelete() {
        val existing = testing.newSavedParent()

        existing.delete()

        expect(testing.allParents().toList()).isEmpty()
        expect {
            existing.version
        }.toThrow<DomainException> { }
        testing.expectDomainChangedEvents()
                .containsExactly(ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 1),
                        null))
    }

    @Test
    fun shouldNotDelete() {
        val parent = testing.newSavedParent()
        val child = testing.newSavedUnassignedChild()

        parent.assign(child)

        expect {
            parent.delete()
        }.toThrow<DomainException> { }
    }

    @Test
    fun shouldNotAssignAlreadyAssignedChild() {
        val parent = testing.newSavedParent()
        val child = testing.newSavedUnassignedChild()

        parent.assign(child)

        expect {
            parent.assign(child)
        }.toThrow<DomainException> { }
    }

    @Test
    fun shouldAssignAndUnassignChild() {
        val parent = testing.newSavedParent()
        val child = testing.newSavedUnassignedChild()

        expect(parent.children).isEmpty()

        val assignedChild = parent.assign(child)
        val parentAssignedWithChild = parent.save().domain

        expect(parent.children).containsExactly(child)
        expect(parentAssignedWithChild.version).toBe(2)
        expect(testing.currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        testing.expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, null, emptySet(), 1),
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, null, emptySet(), 2)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 1),
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 2)))

        parent.unassign(assignedChild)
        val childUnassigned = parent.save().domain

        expect(parent.children).isEmpty()
        expect(childUnassigned.version).toBe(3)
        expect(testing.currentPersistedChild().parentNaturalId).toBe(null)
        testing.expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, null, emptySet(), 2),
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, null, emptySet(), 3)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 2),
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                setOf(), 3)))
    }
}
