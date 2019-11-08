package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.hasSize
import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult

internal class PersistedOthersTest
    : LiveTestBase() {
    @Test
    fun shouldCreateNew() {
        val found = others.findExistingOrCreateNew(otherNaturalId)

        expect(found).toBe(createNewOther())

        expectSqlQueryCountsByType(select = 1)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldFindExisting() {
        val saved = newSavedOther()

        val found = others.findExistingOrCreateNew(otherNaturalId)

        expect(found).toBe(saved)

        expectSqlQueryCountsByType(select = 1)
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = createNewOther()

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
    fun shouldDetectNoChanges() {
        val original = newSavedOther()
        val resaved = original.save()

        expect(resaved).toBe(UpsertedDomainResult(original, false))

        expectSqlQueries().isEmpty()
        expectDomainChangedEvents().isEmpty()
    }

    @Test
    fun shouldMutate() {
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
