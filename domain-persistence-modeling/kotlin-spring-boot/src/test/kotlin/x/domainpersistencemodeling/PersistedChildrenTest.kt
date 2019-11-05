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
internal open class PersistedChildrenTest @Autowired constructor(
        private val testing: Testing) {
    @Test
    fun shouldCreateNew() {
        val found = testing.findExistingOrCreateNewUnassignedChild()

        expect(found).toBe(testing.createNewUnassignedChild())
        expect(found.existing).toBe(false)
    }

    @Test
    fun shouldFindExisting() {
        val saved = testing.newSavedUnassignedChild()

        val found = testing.findExistingOrCreateNewUnassignedChild()

        expect(found).toBe(saved)
        expect(found.existing).toBe(true)
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = testing.createNewUnassignedChild()

        expect(unsaved.version).toBe(0)
        testing.expectDomainChangedEvents().isEmpty()

        val saved = unsaved.save()

        expect(testing.allChildren().toList()).hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        testing.expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                null,
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        null, emptySet(), 1)))

        expect(testing.currentPersistedChild()).toBe(unsaved)
    }

    @Test
    fun shouldDetectNoChanges() {
        val original = testing.newSavedUnassignedChild()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))
        testing.expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldMutate() {
        val original = testing.newSavedUnassignedChild()

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
        testing.expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        null, emptySet(), 1),
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        value, emptySet(), 2)))
    }

    @Test
    fun shouldDelete() {
        val existing = testing.newSavedUnassignedChild()

        existing.delete()

        expect(testing.allChildren().toList()).isEmpty()
        expect {
            existing.version
        }.toThrow<DomainException> { }
        testing.expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        null, emptySet(), 1),
                null))
    }

    @Test
    fun shouldAssignChildAtCreation() {
        val parent = testing.newSavedParent()

        expect(parent.version).toBe(1)

        val unsaved = testing.createNewUnassignedChild()
        unsaved.update {
            assignTo(parent)
        }

        expect(unsaved.parentNaturalId).toBe(parentNaturalId)

        unsaved.save()

        expect(testing.currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        expect(testing.currentPersistedParent().version).toBe(2)
        testing.expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                null,
                ChildSnapshot(childNaturalId, parentNaturalId, ENABLED.name,
                        null, emptySet(), 1)))
    }

    @Test
    fun shouldAssignChildAtMutation() {
        val parent = testing.newSavedParent()
        val child = testing.newSavedUnassignedChild()

        expect(parent.version).toBe(1)

        val assigned = child
        assigned.update {
            assignTo(parent)
        }

        expect(assigned.parentNaturalId).toBe(parentNaturalId)
        testing.expectDomainChangedEvents().isEmpty()

        assigned.save()

        expect(assigned.version).toBe(2)
        expect(testing.currentPersistedChild().parentNaturalId)
                .toBe(parentNaturalId)
        expect(testing.currentPersistedParent().version).toBe(2)
        testing.expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        null, emptySet(), 1),
                ChildSnapshot(childNaturalId, parentNaturalId, ENABLED.name,
                        null, emptySet(), 2)))
    }

    @Test
    fun shouldUnassignChild() {
        val parent = testing.newSavedParent()
        val child = testing.createNewUnassignedChild()
        child.update {
            assignTo(parent)
        }
        child.save().domain
        testing.resetDomainChangedEvents()

        expect(parent.version).toBe(1)

        child.update(MutableChild::unassignFromAny)
        child.save()

        expect(child.version).toBe(2)
        expect(testing.currentPersistedChild().parentNaturalId).toBe(null)
        // Created, assigned by child, unassigned by child == version 3
        expect(testing.currentPersistedParent().version).toBe(3)
        testing.expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, parentNaturalId, ENABLED.name,
                        null, emptySet(), 1),
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        null, emptySet(), 2)))
    }
}
