package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES
import io.micronaut.data.repository.CrudRepository
import x.domainpersistencemodeling.UpsertableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects
import java.util.Optional
import java.util.TreeSet
import javax.inject.Singleton
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Singleton
internal open class PersistedChildFactory(
        private val repository: ChildRepository,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory {
    companion object {
        internal fun toResource(record: ChildRecord) =
                ChildResource(record.naturalId,
                        record.parentNaturalId,
                        record.value,
                        record.subchildren,
                        record.version)
    }

    override fun all(): Sequence<Child> =
            repository.findAll().map {
                toChild(it)
            }.asSequence()

    override fun findExisting(naturalId: String): Child? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                toChild(it)
            }

    override fun createNew(naturalId: String): Child =
            PersistedChild(this, null, ChildRecord(naturalId))

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(naturalId)

    override fun findOwned(parentNaturalId: String) =
            repository.findByParentNaturalId(parentNaturalId).map {
                toChild(it)
            }.asSequence()

    internal fun save(record: ChildRecord) =
            UpsertedRecordResult.of(record, repository.upsert(record))

    internal fun delete(record: ChildRecord) =
            repository.delete(record)

    internal fun notifyChanged(
            before: ChildResource?, after: ChildResource?) =
            publisher.publishEvent(ChildChangedEvent(before, after))

    private fun toChild(record: ChildRecord) =
            PersistedChild(this, toResource(record), record)
}

internal class PersistedChild(
        private val factory: PersistedChildFactory,
        private var snapshot: ChildResource?,
        private var record: ChildRecord?)
    : Child {
    override val naturalId: String
        get() = record().naturalId
    override val parentNaturalId: String?
        get() = record().parentNaturalId
    override val value: String?
        get() = record().value
    override val version: Int
        get() = record().version
    override val subchildren: Set<String> // Sorted
        get() = TreeSet(record().subchildren)

    override val changed
        get() = snapshot != toResource()

    override fun save(): UpsertedDomainResult<Child> {
        if (!changed) return UpsertedDomainResult(this, false)

        val before = snapshot
        val result = factory.save(record())
        record = result.record
        val after = toResource()
        snapshot = after
        if (result.changed) // Trust the database
            factory.notifyChanged(before, after)
        return UpsertedDomainResult(this, result.changed)
    }

    override fun delete() {
        if (null != parentNaturalId) throw DomainException(
                "Deleting child assigned to a parent: $this")

        val before = snapshot
        val after = (null as ChildResource?)
        factory.delete(record())
        record = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun update(block: MutableChild.() -> Unit) = apply {
        val mutable = PersistedMutableChild(record())
        mutable.block()
    }

    override fun toResource() =
            PersistedChildFactory.toResource(record())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedChild
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"

    private fun record() =
            record ?: throw DomainException("Deleted: $this")
}

internal class PersistedMutableChild(private val record: ChildRecord)
    : MutableChild,
        MutableChildDetails by record {
    override val subchildren: MutableSet<String>
        get() = TrackedSortedSet(record.subchildren,
                { _, all -> record.subchildren = all },
                { _, all -> record.subchildren = all })

    override fun assignTo(parent: ParentDetails) {
        record.parentNaturalId = parent.naturalId
    }

    override fun unassignFromAny() {
        record.parentNaturalId = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableChild
        return record == other.record
    }

    override fun hashCode() = Objects.hash(record)

    override fun toString() =
            "${super.toString()}{record=$record}"
}

@JdbcRepository(dialect = POSTGRES)
interface ChildRepository : CrudRepository<ChildRecord, Long> {
    @Query("""
        SELECT *
        FROM child
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(naturalId: String): Optional<ChildRecord>

    @Query("""
        SELECT *
        FROM child
        WHERE parent_natural_id = :parentNaturalId
        """)
    fun findByParentNaturalId(parentNaturalId: String): Iterable<ChildRecord>

    @Query("""
        SELECT *
        FROM upsert_child(:naturalId, :parentNaturalId, :value, :subchildren, :version)
        """)
    fun upsert(naturalId: String, parentNaturalId: String?, value: String?,
            subchildren: String, version: Int)
            : ChildRecord?
}

fun ChildRepository.upsert(entity: ChildRecord): ChildRecord? {
    // TODO: Workaround issue in Spring Data with passing sets for
    //  ARRAY types in a procedure -- What does Micronaut do?
    val upserted = upsert(entity.naturalId,
            entity.parentNaturalId,
            entity.value,
            entity.subchildren.joinToString(",", "{", "}"),
            entity.version)
    if (null != upserted) {
        entity.updateWith(upserted)
    }
    return upserted
}

@Entity
@Introspected
@Table(name = "child")
data class ChildRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var parentNaturalId: String?,
        override var value: String?,
        override var subchildren: MutableSet<String>,
        override var version: Int)
    : MutableChildDetails,
        UpsertableRecord<ChildRecord> {
    internal constructor(naturalId: String)
            : this(null, naturalId, null, null, mutableSetOf(), 0)

    override fun updateWith(upserted: ChildRecord): ChildRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        parentNaturalId = upserted.parentNaturalId
        value = upserted.value
        subchildren = TreeSet(upserted.subchildren)
        version = upserted.version
        return this
    }
}
