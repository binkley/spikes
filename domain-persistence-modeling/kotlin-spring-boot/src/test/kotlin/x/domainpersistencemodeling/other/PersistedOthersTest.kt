package x.domainpersistencemodeling.other

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.hasSize
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.LiveTestBase
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.anOtherChangedEvent
import x.domainpersistencemodeling.otherNaturalId

internal class PersistedOthersTest
    : LiveTestBase() {
    @Test
    fun `should create new`() {
        val found = others.findExistingOrCreateNew(
                otherNaturalId)

        expect(found).toBe(newUnsavedOther())

        expectSqlQueryCountsByType(select = 1)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun `should find existing`() {
        val saved = newSavedOther()

        val found = others.findExistingOrCreateNew(
                otherNaturalId)

        expect(found).toBe(saved)

        expectSqlQueryCountsByType(select = 1)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun `should round trip`() {
        val unsaved = newUnsavedOther()

        expect(unsaved.version).toBe(0)
        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()

        val saved = unsaved.save()

        expectSqlQueryCountsByType(upsert = 1)
        expectAllOthers().hasSize(1)
        expect(unsaved.version).toBe(1)
        expect(saved).toBe(UpsertedDomainResult(unsaved, true))
        expect(currentPersistedOther()).toBe(unsaved)

        expectDomainChangedEvents().containsExactly(
                anOtherChangedEvent(
                        noBefore = true,
                        afterVersion = 1))
    }

    @Test
    fun `should detect no changes`() {
        val original = newSavedOther()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun `should mutate`() {
        val original = newSavedOther()

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
                anOtherChangedEvent(
                        beforeOtherVersion = 1,
                        beforeValue = null,
                        afterVersion = 2,
                        afterValue = value))
    }
}
