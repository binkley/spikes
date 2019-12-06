package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.isEmpty
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import x.domainpersistencemodeling.child.Child
import x.domainpersistencemodeling.child.ChildChangedEvent
import x.domainpersistencemodeling.child.ChildFactory
import x.domainpersistencemodeling.child.ChildRecord
import x.domainpersistencemodeling.child.ChildSnapshot
import x.domainpersistencemodeling.child.PersistedUnassignedChild
import x.domainpersistencemodeling.child.UnassignedChild
import x.domainpersistencemodeling.other.Other
import x.domainpersistencemodeling.other.OtherChangedEvent
import x.domainpersistencemodeling.other.OtherFactory
import x.domainpersistencemodeling.other.OtherRecord
import x.domainpersistencemodeling.other.OtherSnapshot
import x.domainpersistencemodeling.parent.Parent
import x.domainpersistencemodeling.parent.ParentChangedEvent
import x.domainpersistencemodeling.parent.ParentFactory
import x.domainpersistencemodeling.parent.ParentRecord
import x.domainpersistencemodeling.parent.ParentSnapshot
import java.time.Instant.EPOCH
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.util.TimeZone
import javax.inject.Inject

internal const val otherNaturalId = "o"
internal const val parentNaturalId = "p"
internal const val childNaturalId = "c"
internal val atZero = EPOCH.atOffset(UTC)

/**
 * Provides testing help for the full stack application with a Dockerized
 * Postgres on a random port.  Tests are transactional, so forgets any
 * persistence changes made during tests.
 *
 * The general pattern for "newFoo" methods:
 * - Resets the appropriate SQL or domain changed event tracker
 *
 * The general pattern for "expectFoo" methods:
 * - Returns an Atrium expectation natural to the methods
 * - Resets the appropriate SQL or domain changed event tracker
 *
 * The general pattern for "currentFoo" methods:
 * - Resets the appropriate SQL or domain changed event tracker
 */
@MicronautTest
internal abstract class LiveTestBase {
    @Inject
    lateinit var others: OtherFactory
    @Inject
    lateinit var parents: ParentFactory
    @Inject
    lateinit var children: ChildFactory
    @Inject
    lateinit var sqlQueries: SqlQueries
    @Inject
    lateinit var testListener: TestListener<DomainChangedEvent<*>>
    @Inject
    lateinit var programmableListener: ProgrammableListener<DomainChangedEvent<*>>

    init {
        TimeZone.setDefault(TimeZone.getTimeZone(UTC))
    }

    @BeforeEach
    internal fun beforeEach() {
        programmableListener.fail = false
    }

    /**
     * Aggressively checks that tests asserted against SQL queries and domain
     * change events.  Use [@AfterEach] to tie failures to tests that do not
     * check.
     */
    @AfterEach
    internal fun tearDown() {
        sqlQueries.expectNext.isEmpty()
        testListener.expectNext.isEmpty()
    }

    internal fun expectSqlQueries() = sqlQueries.expectNext
    internal fun <V> expectSqlQueriesByType(toValue: (List<String>) -> V) =
        sqlQueries.expectNextByType(toValue)

    internal fun expectSqlQueryCountsByType(
        delete: Int = 0,
        insert: Int = 0,
        select: Int = 0,
        update: Int = 0,
        upsert: Int = 0
    ) {
        val expected = mutableMapOf<String, Int>()
        if (0 != delete) expected["DELETE"] = delete
        if (0 != insert) expected["INSERT"] = insert
        if (0 != select) expected["SELECT"] = select
        if (0 != update) expected["UPDATE"] = update
        if (0 != upsert) expected["UPSERT"] = upsert
        expectSqlQueriesByType {
            it.size
        }.toBe(expected)
    }

    internal fun expectDomainChangedEvents() = testListener.expectNext

    internal fun expectAllOthers() = expect(others.all().toList()).also {
        sqlQueries.reset()
    }

    internal fun newUnsavedOther(naturalId: String = otherNaturalId): Other =
        others.createNew(naturalId)

    internal fun newSavedOther(): Other {
        val saved = newUnsavedOther().save()
        expect(saved.changed).toBe(true)
        val other = saved.domain
        sqlQueries.reset()
        testListener.reset()
        return other
    }

    internal fun currentPersistedOther(
        naturalId: String = otherNaturalId
    ): Other =
        others.findExisting(naturalId)!!.also {
            sqlQueries.reset()
        }

    internal fun expectAllParents() = expect(parents.all().toList()).also {
        sqlQueries.reset()
    }

    internal fun newUnsavedParent(
        naturalId: String = parentNaturalId
    ): Parent =
        parents.createNew(naturalId)

    internal fun newSavedParent(): Parent {
        val saved = newUnsavedParent().save()
        expect(saved.changed).toBe(true)
        val parent = saved.domain
        sqlQueries.reset()
        testListener.reset()
        return parent
    }

    internal fun currentPersistedParent(
        naturalId: String = parentNaturalId
    ): Parent =
        parents.findExisting(naturalId)!!.also {
            sqlQueries.reset()
        }

    internal fun expectAllChildren() = expect(children.all().toList()).also {
        sqlQueries.reset()
    }

    internal fun newSavedUnassignedChild(): UnassignedChild {
        val saved = newUnsavedUnassignedChild().save()
        expect(saved.changed).toBe(true)
        val child = saved.domain
        sqlQueries.reset()
        testListener.reset()
        return child as PersistedUnassignedChild
    }

    internal fun currentPersistedChild(
        naturalId: String = childNaturalId
    ): Child<*> =
        children.findExisting(naturalId)!!.also {
            sqlQueries.reset()
        }

    internal fun newUnsavedUnassignedChild(
        naturalId: String = childNaturalId
    ): UnassignedChild =
        children.createNewUnassigned(naturalId)
                as PersistedUnassignedChild
}

private val otherRecord = OtherRecord(otherNaturalId)

internal fun anOtherChangedEvent(
    noBefore: Boolean = false,
    beforeValue: String? = otherRecord.value,
    beforeVersion: Int = otherRecord.version,
    noAfter: Boolean = false,
    afterValue: String? = otherRecord.value,
    afterVersion: Int = otherRecord.version
) =
    OtherChangedEvent(
        if (noBefore) null else OtherSnapshot(
            otherNaturalId, beforeValue,
            beforeVersion
        ),
        if (noAfter) null else OtherSnapshot(
            otherNaturalId, afterValue,
            afterVersion
        )
    )

private val parentRecord = ParentRecord(parentNaturalId)

internal fun aParentChangedEvent(
    noBefore: Boolean = false,
    beforeOtherNaturalId: String? = parentRecord.otherNaturalId,
    beforeState: String = parentRecord.state,
    beforeAt: OffsetDateTime? = null, // TODO: Should parents have an "at"?
    beforeValue: String? = parentRecord.value,
    beforeSideValues: Set<String> = parentRecord.sideValues,
    beforeVersion: Int = parentRecord.version,
    noAfter: Boolean = false,
    afterOtherNaturalId: String? = parentRecord.otherNaturalId,
    afterState: String = parentRecord.state,
    afterAt: OffsetDateTime? = null, // TODO: Should parents have an "at"?
    afterValue: String? = parentRecord.value,
    afterSideValues: Set<String> = parentRecord.sideValues,
    afterVersion: Int = parentRecord.version
) =
    ParentChangedEvent(
        if (noBefore) null else ParentSnapshot(
            parentNaturalId,
            beforeOtherNaturalId, beforeState, beforeAt,
            beforeValue,
            beforeSideValues, beforeVersion
        ),
        if (noAfter) null else ParentSnapshot(
            parentNaturalId,
            afterOtherNaturalId, afterState, afterAt, afterValue,
            afterSideValues, afterVersion
        )
    )

private val childRecord = ChildRecord(childNaturalId)

internal fun aChildChangedEvent(
    noBefore: Boolean = false,
    beforeOtherNaturalId: String? = childRecord.otherNaturalId,
    beforeParentNaturalId: String? = childRecord.parentNaturalId,
    beforeState: String = childRecord.state,
    beforeAt: OffsetDateTime = childRecord.at,
    beforeValue: String? = childRecord.value,
    beforeDefaultSideValues: Set<String> = childRecord.defaultSideValues,
    beforeSideValues: Set<String> = childRecord.sideValues,
    beforeVersion: Int = childRecord.version,
    noAfter: Boolean = false,
    afterOtherNaturalId: String? = childRecord.otherNaturalId,
    afterParentNaturalId: String? = childRecord.parentNaturalId,
    afterState: String = childRecord.state,
    afterAt: OffsetDateTime = childRecord.at,
    afterValue: String? = childRecord.value,
    afterDefaultSideValues: Set<String> = childRecord.defaultSideValues,
    afterSideValues: Set<String> = childRecord.sideValues,
    afterVersion: Int = childRecord.version
) =
    ChildChangedEvent(
        if (noBefore) null else ChildSnapshot(
            childNaturalId,
            beforeOtherNaturalId,
            beforeParentNaturalId,
            beforeState,
            beforeAt,
            beforeValue,
            beforeDefaultSideValues,
            beforeSideValues,
            beforeVersion
        ),
        if (noAfter) null else ChildSnapshot(
            childNaturalId,
            afterOtherNaturalId,
            afterParentNaturalId,
            afterState,
            afterAt,
            afterValue,
            afterDefaultSideValues,
            afterSideValues,
            afterVersion
        )
    )
