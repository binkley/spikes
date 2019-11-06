package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.hasSize
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult

internal class PersistedParentsTest
    : LiveTestBase() {
    @Test
    fun shouldCreateNew() {
        val found = findExistingOrCreateNewParent()

        expect(found).toBe(createNewParent())
        expect(found.children).isEmpty()
    }

    @Test
    fun shouldFindExisting() {
        val saved = newSavedParent()

        val found = findExistingOrCreateNewParent()

        expect(found).toBe(saved)
        expect(found.children).isEmpty()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = createNewParent()

        expect(unsaved.version).toBe(0)
        expectDomainChangedEvents().isEmpty()

        val saved = unsaved.save()

        expect(allParents().toList()).hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        expectDomainChangedEvents().containsExactly(
                ParentChangedEvent(
                        null,
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 1)))

        expect(currentPersistedParent()).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChanges() {
        val original = newSavedParent()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))
        expectDomainChangedEvents().isEmpty()
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
        expectDomainChangedEvents().isEmpty()

        original.save()

        expect(original.changed).toBe(false)
        expectDomainChangedEvents().containsExactly(
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 1),
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                value, setOf(), 2)))
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

        expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, atZero, null, emptySet(), 1),
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, atZero, value, emptySet(), 2)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 1),
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 2)))
    }

    @Test
    fun shouldDelete() {
        val existing = newSavedParent()

        existing.delete()

        expect(allParents().toList()).isEmpty()
        expect {
            existing.version
        }.toThrow<DomainException> { }
        expectDomainChangedEvents().containsExactly(
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 1),
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
        expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, atZero, null, emptySet(), 1),
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, atZero, null, emptySet(), 2)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 1),
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 2)))

        parent.unassign(assignedChild)
        val childUnassigned = parent.save().domain

        expect(parent.children).isEmpty()
        expect(childUnassigned.version).toBe(3)
        expect(currentPersistedChild().parentNaturalId).toBe(null)
        expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, atZero, null, emptySet(), 2),
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, atZero, null, emptySet(), 3)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 2),
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 3)))
    }
}
