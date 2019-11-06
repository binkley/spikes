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

internal class PersistedChildrenTest
    : LiveTestBase() {
    @Test
    fun shouldCreateNew() {
        val found = findExistingOrCreateNewUnassignedChild()

        expect(found).toBe(createNewUnassignedChild())
        expect(found.existing).toBe(false)
    }

    @Test
    fun shouldFindExisting() {
        val saved = newSavedUnassignedChild()

        val found = findExistingOrCreateNewUnassignedChild()

        expect(found).toBe(saved)
        expect(found.existing).toBe(true)
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = createNewUnassignedChild()

        expect(unsaved.version).toBe(0)
        expectDomainChangedEvents().isEmpty()

        val saved = unsaved.save()

        expect(allChildren().toList()).hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                null,
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        atZero, null, emptySet(), 1)))

        expect(currentPersistedChild()).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChanges() {
        val original = newSavedUnassignedChild()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldMutate() {
        val original = newSavedUnassignedChild()

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
        expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        atZero, null, emptySet(), 1),
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        atZero, value, emptySet(), 2)))
    }

    @Test
    fun shouldDelete() {
        val existing = newSavedUnassignedChild()

        existing.delete()

        expect(allChildren().toList()).isEmpty()
        expect {
            existing.version
        }.toThrow<DomainException> { }
        expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        atZero, null, emptySet(), 1),
                null))
    }

    @Test
    fun shouldAssignChildAtCreation() {
        val parent = newSavedParent()

        expect(parent.version).toBe(1)

        val unsaved = createNewUnassignedChild()
        unsaved.update {
            assignTo(parent)
        }

        expect(unsaved.parentNaturalId).toBe(parentNaturalId)

        unsaved.save()

        expect(currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        expect(currentPersistedParent().version).toBe(2)
        expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                null,
                ChildSnapshot(childNaturalId, parentNaturalId, ENABLED.name,
                        atZero, null, emptySet(), 1)))
    }

    @Test
    fun shouldAssignChildAtMutation() {
        val parent = newSavedParent()
        val child = newSavedUnassignedChild()

        expect(parent.version).toBe(1)

        val assigned = child
        assigned.update {
            assignTo(parent)
        }

        expect(assigned.parentNaturalId).toBe(parentNaturalId)
        expectDomainChangedEvents().isEmpty()

        assigned.save()

        expect(assigned.version).toBe(2)
        expect(currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        expect(currentPersistedParent().version).toBe(2)
        expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        atZero, null, emptySet(), 1),
                ChildSnapshot(childNaturalId, parentNaturalId, ENABLED.name,
                        atZero, null, emptySet(), 2)))
    }

    @Test
    fun shouldUnassignChild() {
        val parent = newSavedParent()
        val child = createNewUnassignedChild()
        child.update {
            assignTo(parent)
        }
        child.save().domain
        resetDomainChangedEvents()

        expect(parent.version).toBe(1)

        child.update(MutableChild::unassignFromAny)
        child.save()

        expect(child.version).toBe(2)
        expect(currentPersistedChild().parentNaturalId).toBe(null)
        // Created, assigned by child, unassigned by child == version 3
        expect(currentPersistedParent().version).toBe(3)
        expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, parentNaturalId, ENABLED.name,
                        atZero, null, emptySet(), 1),
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        atZero, null, emptySet(), 2)))
    }
}
