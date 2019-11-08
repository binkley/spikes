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
        val found = parents.findExistingOrCreateNew(parentNaturalId)

        expect(found).toBe(createNewParent())
        expect(found.children).isEmpty()

        expectSqlQueryCountsByType(select = 1)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldFindExisting() {
        val saved = newSavedParent()

        val found = parents.findExistingOrCreateNew(parentNaturalId)

        expect(found).toBe(saved)
        expect(found.children).isEmpty()

        // 1 == parent, 2 == children (none)
        expectSqlQueryCountsByType(select = 2)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = createNewParent()

        expect(unsaved.version).toBe(0)
        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()

        val saved = unsaved.save()

        expectSqlQueryCountsByType(upsert = 1)
        expectAllParents().hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        expect(currentPersistedParent()).toBe(unsaved)

        expectDomainChangedEvents().containsExactly(
                ParentChangedEvent(
                        null,
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 1)))
    }

    @Test
    fun shouldDetectNoChanges() {
        val original = newSavedParent()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))

        expectSqlQueries().isEmpty()
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

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()

        original.save()

        expectSqlQueryCountsByType(upsert = 1)

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

        expect(parent.at).toBe(null)

        val child = newSavedUnassignedChild()

        parent.assign(child)

        expect(parent.at).toBe(child.at)

        // TODO: Millis and micros work, but not nanos
        val at = atZero.plusNanos(1_000L)
        val value = "FOOBAR"
        parent.update {
            children.forEach {
                it.update {
                    this.at = at
                    this.value = value
                }
            }
        }
        parent.save()

        expectSqlQueryCountsByType(select = 1, upsert = 2)

        expect(currentPersistedChild().at).toBe(at)
        expect(currentPersistedChild().value).toBe(value)
        expect(currentPersistedParent().at).toBe(at)

        expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, atZero, null, emptySet(), 1),
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, at, value, emptySet(), 2)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 1),
                        ParentSnapshot(parentNaturalId, ENABLED.name, at,
                                null, setOf(), 2)))
    }

    @Test
    fun shouldDelete() {
        val existing = newSavedParent()

        existing.delete()

        expectSqlQueryCountsByType(delete = 1)

        expectAllParents().isEmpty()
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

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldNotAssignAlreadyAssignedChild() {
        val parent = newSavedParent()
        val child = newSavedUnassignedChild()

        parent.assign(child)

        expect {
            parent.assign(child)
        }.toThrow<DomainException> { }

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldAssignAndUnassignChild() {
        val parent = newSavedParent()
        val child = newSavedUnassignedChild()

        expect(parent.children).isEmpty()

        val assignedChild = parent.assign(child)
        val parentAssignedWithChild = parent.save().domain

        expectSqlQueryCountsByType(select = 1, upsert = 2)

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
                        ParentSnapshot(parentNaturalId, ENABLED.name, atZero,
                                null, setOf(), 2)))

        parent.unassign(assignedChild)
        val childUnassigned = parent.save().domain

        expectSqlQueryCountsByType(select = 1, upsert = 2)

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
                        ParentSnapshot(parentNaturalId, ENABLED.name, atZero,
                                null, setOf(), 2),
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 3)))
    }

    @Test
    fun shouldPersistMutatedButUnassignedChildren() {
        val parent = newSavedParent()
        val child = newSavedUnassignedChild()
        val assignedChild = parent.assign(child)

        parent.save()

        expectSqlQueryCountsByType(select = 1, upsert = 2)
        expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, atZero, null, emptySet(), 1),
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, atZero, null, emptySet(), 2)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, null,
                                null, setOf(), 1),
                        ParentSnapshot(parentNaturalId, ENABLED.name, atZero,
                                null, setOf(), 2)))

        val value = "PQR"
        child.update {
            this.value = value
        }

        parent.unassign(assignedChild)
        parent.delete()

        expect(currentPersistedChild().value).toBe(value)

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().containsExactly(
                ChildChangedEvent(
                        ChildSnapshot(childNaturalId, parentNaturalId,
                                ENABLED.name, atZero, null, emptySet(), 2),
                        ChildSnapshot(childNaturalId, null,
                                ENABLED.name, atZero, value, emptySet(), 3)),
                ParentChangedEvent(
                        ParentSnapshot(parentNaturalId, ENABLED.name, atZero,
                                null, setOf(), 2),
                        null))
    }
}
