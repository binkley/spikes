package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES
import io.micronaut.data.repository.CrudRepository
import java.time.Instant
import java.time.Instant.EPOCH
import java.util.*
import javax.inject.Singleton
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Singleton
internal class PersistedChildFactory(
        private val repository: ChildRepository,
        private val parentFactory: PersistedParentFactory,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory {
    override fun all(): Sequence<Child> = repository.findAll().map {
        forRecord(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Child? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                forRecord(it)
            }

    override fun createNew(naturalId: String): Child =
            PersistedChild(null, ChildRecord(naturalId, null), this)

    override fun findExistingOrCreateNew(
            naturalId: String): Child =
            findExisting(naturalId) ?: createNew(naturalId)

    override fun findOwned(parentNaturalId: String) =
            repository.findByParentNaturalId(parentNaturalId).map {
                forRecord(it)
            }

    // TODO: Refetch to see changes in audit columns
    internal fun save(record: ChildRecord) =
            repository.findByNaturalId(
                    repository.save(record).naturalId).get()

    internal fun delete(record: ChildRecord) =
            repository.delete(record)

    internal fun notifyChanged(
            before: ChildResource?,
            after: ChildResource?) =
            notifyIfChanged(before, after,
                    publisher,
                    ::ChildChangedEvent)

    internal fun parentNaturalIdFor(parentId: Long) =
            parentFactory.naturalIdFor(parentId)

    internal fun parentIdFor(parent: Parent) =
            parentFactory.idFor(parent.naturalId)

    private fun forRecord(record: ChildRecord): PersistedChild {
        val parentId = record.parentId
        val resource = ChildResource(
                record.naturalId,
                parentId?.let {
                    parentNaturalIdFor(it)
                },
                record.value,
                record.subchildren,
                record.version)
        return PersistedChild(resource, record, this)
    }
}

internal class PersistedChild internal constructor(
        private var snapshot: ChildResource?,
        private var record: ChildRecord?,
        private val factory: PersistedChildFactory)
    : Child {
    override val naturalId: String
        get() = record!!.naturalId
    override val parentNaturalId: String?
        get() = record!!.parentId?.let {
            factory.parentNaturalIdFor(it)
        }
    override val value: String?
        get() = record!!.value
    override val subchildren: Set<String> // Sorted
        get() = TreeSet(record!!.subchildren)
    override val version: Int
        get() = record!!.version
    override val existing: Boolean
        get() = 0 < version

    override fun update(block: MutableChild.() -> Unit) = apply {
        val mutable = PersistedMutableChild(record!!, factory)
        mutable.block()
    }

    override fun save() = apply {
        val before = snapshot
        record = factory.save(record!!)
        val after = toResource()
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun delete() {
        val before = snapshot
        val after = null
        factory.delete(record!!)
        record = null
        snapshot = null
        factory.notifyChanged(before, after)
    }

    override fun compareTo(other: Child) =
            naturalId.compareTo(other.naturalId)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedChild
        return snapshot == other.snapshot
                && record == other.record
    }

    internal fun toResource() = ChildResource(
            naturalId, parentNaturalId, value, subchildren, version)

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

internal class PersistedMutableChild internal constructor(
        private val record: ChildRecord,
        private val factory: PersistedChildFactory)
    : MutableChild,
        MutableChildDetails by record {
    override var subchildren =
            TrackedSortedSet(record.subchildren,
                    ::resetSubchildrenToPreserveSorting,
                    ::resetSubchildrenToPreserveSorting)

    override fun assignTo(parent: Parent) = run {
        record.parentId = factory.parentIdFor(parent)
    }

    override fun unassignFromAny() = run {
        record.parentId = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableChild
        return record == other.record
    }

    override fun hashCode() = Objects.hash(record)

    override fun toString() = "${super.toString()}{record=$record}"

    private fun resetSubchildrenToPreserveSorting(
            item: String, all: Set<String>) {
        record.subchildren.clear()
        record.subchildren.addAll(all)
    }
}

@JdbcRepository(dialect = POSTGRES)
interface ChildRepository : CrudRepository<ChildRecord, Long> {
    // TODO: Can I return Kotlin `ChildRecord?`
    fun findByNaturalId(naturalId: String): Optional<ChildRecord>

    @Query("""
        SELECT * FROM child
        WHERE parent_id = (SELECT id 
        FROM parent 
        WHERE natural_id = :parentNaturalId)
        """)
    fun findByParentNaturalId(parentNaturalId: String): Iterable<ChildRecord>
}

@Introspected
@Table(name = "child")
data class ChildRecord(
        @Id @GeneratedValue val id: Long?,
        override val naturalId: String,
        override var parentId: Long?,
        override var value: String?,
        override var subchildren: MutableSet<String>,
        override val version: Int,
        val createdAt: Instant,
        val updatedAt: Instant) :
        MutableChildDetails {
    internal constructor(naturalId: String, parentId: Long?)
            : this(null, naturalId, parentId, null, mutableSetOf(),
            0, EPOCH, EPOCH)
}
