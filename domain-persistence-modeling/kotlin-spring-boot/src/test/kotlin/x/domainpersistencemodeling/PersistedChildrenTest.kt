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
        val found = children.findExistingOrCreateNewUnassigned(childNaturalId)

        expect(found).toBe(createNewUnassignedChild())
        expect(found.existing).toBe(false)

        expectSqlQueryCountsByType(select = 1)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldFindExisting() {
        val saved = newSavedUnassignedChild()

        val found = children.findExistingOrCreateNewUnassigned(childNaturalId)

        expect(found).toBe(saved)
        expect(found.existing).toBe(true)

        expectSqlQueryCountsByType(select = 1)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = createNewUnassignedChild()

        expect(unsaved.version).toBe(0)

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()

        val saved = unsaved.save()

        expectSqlQueryCountsByType(upsert = 1)

        expectAllChildren().hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        expect(currentPersistedChild()).toBe(unsaved)

        expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                null,
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        atZero, null, emptySet(), 1)))
    }

    @Test
    fun shouldDetectNoChanges() {
        val original = newSavedUnassignedChild()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldMutate() {
        val original = newSavedUnassignedChild()

        expect(original.changed).toBe(false)

        // TODO: Millis and micros work, but not nanos
        val at = atZero.plusNanos(1_000L)
        val value = "FOOBAR"
        original.update {
            this.at = at
            this.value = value
        }

        expect(original.changed).toBe(true)
        expect(original.at).toBe(at)
        expect(original.value).toBe(value)

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()

        original.save()

        expectSqlQueryCountsByType(upsert = 1)
        expect(original.changed).toBe(false)

        expectDomainChangedEvents().containsExactly(ChildChangedEvent(
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        atZero, null, emptySet(), 1),
                ChildSnapshot(childNaturalId, null, ENABLED.name,
                        at, value, emptySet(), 2)))
    }

    @Test
    fun shouldDelete() {
        val existing = newSavedUnassignedChild()

        existing.delete()

        expectSqlQueryCountsByType(delete = 1)
        expectAllChildren().isEmpty()
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

        expectSqlQueryCountsByType(upsert = 1)

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

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()

        assigned.save()

        expectSqlQueryCountsByType(upsert = 1)

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

        expectSqlQueryCountsByType(upsert = 1)
        expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        null,
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, atZero, null, emptySet(), 1)))

        expect(parent.version).toBe(1)

        child.update(MutableChild::unassignFromAny)
        child.save()

        expectSqlQueryCountsByType(upsert = 1)

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
