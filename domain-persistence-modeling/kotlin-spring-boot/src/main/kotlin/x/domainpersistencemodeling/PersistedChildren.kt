package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects
import java.util.Optional
import java.util.TreeSet

internal fun ChildRecord.toSnapshot() = ChildSnapshot(
        naturalId, parentNaturalId, value, sideValues, version)

@Component
internal open class PersistedChildFactory(
        private val repository: ChildRepository,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory {
    override fun all(): Sequence<Child> =
            repository.findAll().map {
                toChild(it)
            }.asSequence()

    override fun findExisting(naturalId: String): Child? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                toChild(it)
            }

    override fun createNew(naturalId: String): UnassignedChild =
            PersistedChild(this, null, ChildRecord(naturalId))

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(naturalId)

    override fun findOwned(parentNaturalId: String): Sequence<AssignedChild> =
            repository.findByParentNaturalId(parentNaturalId).map {
                toChild(it)
            }.asSequence()

    internal fun save(record: ChildRecord) =
            UpsertedRecordResult.of(record, repository.upsert(record))

    internal fun delete(record: ChildRecord) =
            repository.delete(record)

    internal fun notifyChanged(
            before: ChildSnapshot?, after: ChildSnapshot?) =
            publisher.publishEvent(ChildChangedEvent(before, after))

    private fun toChild(record: ChildRecord) =
            PersistedChild(this, record.toSnapshot(), record)
}

internal open class PersistedChild(
        private val factory: PersistedChildFactory,
        private var snapshot: ChildSnapshot?,
        private var record: ChildRecord?)
    : Child,
        UnassignedChild,
        AssignedChild {
    override val naturalId: String
        get() = record().naturalId
    override val parentNaturalId: String?
        get() = record().parentNaturalId
    override val value: String?
        get() = record().value
    override val sideValues: Set<String> // Sorted
        get() = TreeSet(record().sideValues)
    override val defaultSideValues: Set<String> // Sorted
        get() = TreeSet(record().defaultSideValues)
    override val version: Int
        get() = record().version

    @Transactional
    override fun assignTo(parent: Parent): AssignedChild = apply {
        update {
            assignTo(parent)
        }
    }

    @Transactional
    override fun unassignFromAny(): UnassignedChild = apply {
        update {
            unassignFromAny()
        }
    }

    override val changed
        get() = snapshot != toSnapshot()

    override fun save(): UpsertedDomainResult<ChildSnapshot, Child> {
        if (!changed) return UpsertedDomainResult(this, false)

        val before = snapshot
        val result = factory.save(record())
        record = result.record
        val after = toSnapshot()
        snapshot = after
        if (result.changed) // Trust the database
            factory.notifyChanged(before, after)
        return UpsertedDomainResult(this, result.changed)
    }

    override fun delete() {
        if (null != parentNaturalId) throw DomainException(
                "Deleting child assigned to a parent: $this")

        val before = snapshot
        val after = (null as ChildSnapshot?)
        factory.delete(record())
        record = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun <R> update(block: MutableChild.() -> R): R =
            PersistedMutableChild(record()).let(block)

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

    private fun toSnapshot() = record().toSnapshot()
}

internal class PersistedMutableChild(private val record: ChildRecord)
    : MutableChild,
        MutableChildDetails by record {
    override val sideValues: MutableSet<String>
        get() = TrackedSortedSet(record.sideValues,
                { _, all -> record.sideValues = all },
                { _, all -> record.sideValues = all })

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

@Repository
interface ChildRepository : CrudRepository<ChildRecord, Long> {
    @Query("""
        SELECT *
        FROM child
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<ChildRecord>

    @Query("""
        SELECT *
        FROM child
        WHERE parent_natural_id = :parentNaturalId
        """)
    fun findByParentNaturalId(
            @Param("parentNaturalId") parentNaturalId: String)
            : Iterable<ChildRecord>

    @Query("""
        SELECT *
        FROM upsert_child(:naturalId, :parentNaturalId, :value, 
        :sideValues, :defaultSideValues, :version)
        """)
    fun upsert(
            @Param("naturalId") naturalId: String,
            @Param("parentNaturalId") parentNaturalId: String?,
            @Param("value") value: String?,
            @Param("sideValues") sideValues: String,
            @Param("defaultSideValues") defaultSideValues: String,
            @Param("version") version: Int)
            : Optional<ChildRecord>

    @JvmDefault
    fun upsert(entity: ChildRecord): Optional<ChildRecord> {
        val upserted = upsert(entity.naturalId,
                entity.parentNaturalId,
                entity.value,
                entity.sideValues.workAroundArrayTypeForPostgres(),
                entity.defaultSideValues.workAroundArrayTypeForPostgres(),
                entity.version)
        upserted.ifPresent {
            entity.upsertedWith(it)
        }
        return upserted
    }
}

@Table("child")
data class ChildRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var parentNaturalId: String?,
        override var value: String?,
        override var sideValues: MutableSet<String>,
        override var defaultSideValues: MutableSet<String>,
        override var version: Int)
    : MutableChildDetails,
        UpsertableRecord<ChildRecord> {
    internal constructor(naturalId: String)
            : this(null, naturalId, null, null,
            mutableSetOf(), mutableSetOf(), 0)

    override fun upsertedWith(upserted: ChildRecord): ChildRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        parentNaturalId = upserted.parentNaturalId
        value = upserted.value
        sideValues = TreeSet(upserted.sideValues)
        defaultSideValues = TreeSet(upserted.defaultSideValues)
        version = upserted.version
        return this
    }
}
