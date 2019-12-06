package x.domainpersistencemodeling.parent

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.hasSize
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.isNotEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.DomainException
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.LiveTestBase
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.aChildChangedEvent
import x.domainpersistencemodeling.aParentChangedEvent
import x.domainpersistencemodeling.atZero
import x.domainpersistencemodeling.otherNaturalId
import x.domainpersistencemodeling.parentNaturalId

internal class PersistedParentsTest
    : LiveTestBase() {
    @Test
    fun `should create new`() {
        val found = parents.findExistingOrCreateNew(parentNaturalId)

        expect(found).toBe(newUnsavedParent())
        expect(found.children).isEmpty()

        expectSqlQueryCountsByType(select = 1)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun `should find existing`() {
        val saved = newSavedParent()

        val found = parents.findExistingOrCreateNew(parentNaturalId)

        expect(found).toBe(saved)
        expect(found.children).isEmpty()

        // #1 == parent, #2 == other (none), #3 == children (none)
        expectSqlQueryCountsByType(select = 3)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun `should round trip`() {
        val unsaved = newUnsavedParent()

        expect(unsaved.version).toBe(0)
        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()

        val saved = unsaved.save()

        expectSqlQueryCountsByType(upsert = 1)
        expectAllParents().hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        expect(currentPersistedParent()).toBe(saved.domain)

        expectDomainChangedEvents().containsExactly(
            aParentChangedEvent(
                noBefore = true,
                afterVersion = 1
            )
        )
    }

    @Test
    fun `should detect no changes`() {
        val original = newSavedParent()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun `should mutate`() {
        val original = newSavedParent()

        expect(original.changed).toBe(false)

        val state = "FUNKY"
        val value = "FOOBAR"
        val sideValue = "ABC"
        original.update {
            this.state = state
            this.value = value
            this.sideValues += "FOO"
            this.sideValues += sideValue
            this.sideValues -= "FOO"
        }

        expect(original.changed).toBe(true)
        expect(original.state).toBe(state)
        expect(original.value).toBe(value)
        expect(original.sideValues).containsExactly(sideValue)

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()

        original.save()

        expectSqlQueryCountsByType(upsert = 1)

        expect(original.changed).toBe(false)

        expectDomainChangedEvents().containsExactly(
            aParentChangedEvent(
                beforeVersion = 1,
                beforeState = ENABLED.name,
                beforeValue = null,
                beforeSideValues = setOf(),
                afterVersion = 2,
                afterState = state,
                afterValue = value,
                afterSideValues = setOf(sideValue)
            )
        )
    }

    @Test
    fun `should mutate children`() {
        val parent = newSavedParent()

        expect(parent.at).toBe(null)

        val child = newSavedUnassignedChild()

        parent.assign(child)

        expect(parent.at).toBe(child.at)

        val at = atZero.plusDays(1L)
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
            aChildChangedEvent(
                beforeVersion = 1,
                beforeParentNaturalId = null,
                beforeAt = atZero,
                beforeValue = null,
                afterVersion = 2,
                afterParentNaturalId = parentNaturalId,
                afterAt = at,
                afterValue = value
            ),
            aParentChangedEvent(
                beforeVersion = 1,
                beforeAt = null,
                afterVersion = 2,
                afterAt = at
            )
        )
    }

    @Test
    fun `should delete`() {
        val existing = newSavedParent()

        existing.delete()

        expectSqlQueryCountsByType(delete = 1)

        expectAllParents().isEmpty()
        expect {
            existing.version
        }.toThrow<DomainException> { }

        expectDomainChangedEvents().containsExactly(
            aParentChangedEvent(
                beforeVersion = 1,
                noAfter = true
            )
        )
    }

    @Test
    fun `should not delete`() {
        val parent = newSavedParent()
        val unassigned = newSavedUnassignedChild()

        parent.assign(unassigned)

        expect {
            parent.delete()
        }.toThrow<DomainException> { }

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun `should sort by natural id`() {
        val a = parents.findExistingOrCreateNew(parentNaturalId)
        val b = parents.findExistingOrCreateNew(parentNaturalId + "X")
        val set = sortedSetOf<Parent>()

        set += b
        set += a

        expectSqlQueries().isNotEmpty()
        expect(set).containsExactly(a, b)
    }

    @Test
    fun `should revert on failed delete`() {
        val existing = newSavedParent()

        programmableListener.fail = true

        expect {
            existing.delete()
        }.toThrow<DomainException> { }

        expectSqlQueryCountsByType(delete = 1)

        testListener.reset() // Order of listeners not predictable
        expect(existing.changed).toBe(false)
    }

    @Test
    fun `should not assign already assigned child`() {
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
    fun `should assign and unassign child`() {
        var parent = newSavedParent()
        val unassigned = newSavedUnassignedChild()

        expect(parent.children).isEmpty()
        expect(unassigned.assigned).toBe(false)

        val assigned = parent.assign(unassigned)

        expect(parent.children).containsExactly(assigned)
        expect(assigned.assigned).toBe(true)

        parent = parent.save().domain

        expectSqlQueryCountsByType(select = 1, upsert = 2)

        expect(parent.version).toBe(2)
        expect(currentPersistedChild().parentNaturalId)
            .toBe(parentNaturalId)

        expectDomainChangedEvents().containsExactly(
            aChildChangedEvent(
                beforeVersion = 1,
                beforeParentNaturalId = null,
                afterVersion = 2,
                afterParentNaturalId = parentNaturalId
            ),
            aParentChangedEvent(
                beforeVersion = 1,
                beforeAt = null,
                afterVersion = 2,
                afterAt = atZero
            )
        )

        parent.unassign(assigned)
        parent = parent.save().domain

        expectSqlQueryCountsByType(select = 1, upsert = 2)

        expect(parent.children).isEmpty()
        expect(parent.version).toBe(3)
        expect(currentPersistedChild().parentNaturalId).toBe(null)
        // TODO: Smell -- invalidate an AssignedChild without parent
        expect(assigned.assigned).toBe(false)

        expectDomainChangedEvents().containsExactly(
            aChildChangedEvent(
                beforeVersion = 2,
                beforeParentNaturalId = parentNaturalId,
                afterVersion = 3,
                afterParentNaturalId = null
            ),
            aParentChangedEvent(
                beforeVersion = 2,
                beforeAt = atZero,
                afterVersion = 3,
                afterAt = null
            )
        )
    }

    @Test
    fun `should persist mutated but unassigned children`() {
        val parent = newSavedParent()
        val unassigned = newSavedUnassignedChild()
        val assigned = parent.assign(unassigned)

        parent.save()

        expectSqlQueryCountsByType(select = 1, upsert = 2)
        expectDomainChangedEvents().containsExactly(
            aChildChangedEvent(
                beforeVersion = 1,
                beforeParentNaturalId = null,
                afterVersion = 2,
                afterParentNaturalId = parentNaturalId
            ),
            aParentChangedEvent(
                beforeVersion = 1,
                beforeAt = null,
                afterVersion = 2,
                afterAt = atZero
            )
        )

        val value = "PQR"
        unassigned.update {
            this.value = value
        }

        parent.unassign(assigned)
        parent.delete()

        expect(currentPersistedChild().value).toBe(value)

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().containsExactly(
            aChildChangedEvent(
                beforeVersion = 2,
                beforeParentNaturalId = parentNaturalId,
                beforeValue = null,
                afterVersion = 3,
                afterParentNaturalId = null,
                afterValue = value
            ),
            aParentChangedEvent(
                beforeVersion = 2,
                beforeAt = atZero,
                noAfter = true
            )
        )
    }

    @Test
    fun `should assign and unassign other`() {
        val parent = newSavedParent()
        val other = newSavedOther()

        parent.assign(other)
        parent.save()

        expectSqlQueryCountsByType(select = 1, upsert = 1)
        expect(currentPersistedParent().other).toBe(other)

        expectDomainChangedEvents().containsExactly(
            aParentChangedEvent(
                beforeVersion = 1,
                beforeOtherNaturalId = null,
                afterVersion = 2,
                afterOtherNaturalId = otherNaturalId
            )
        )

        parent.unassignAnyOther()
        parent.save()

        expectSqlQueryCountsByType(select = 1, upsert = 1)
        expect(currentPersistedParent().other).toBe(null)

        expectDomainChangedEvents().containsExactly(
            aParentChangedEvent(
                beforeVersion = 2,
                beforeOtherNaturalId = otherNaturalId,
                afterVersion = 3,
                afterOtherNaturalId = null
            )
        )
    }

    @Test
    fun `should inherit "at" from children`() {
        val child = newUnsavedUnassignedChild()
        val parent = newUnsavedParent()

        child.update {
            at = atZero
        }
        parent.assign(child)

        parent.update {
            expect(at).toBe(atZero)
        }

        expect(parent.at).toBe(atZero)
    }
}
