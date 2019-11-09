package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects
import java.util.Optional

internal fun OtherRecord.toSnapshot() =
        OtherSnapshot(naturalId, value, version)

@Component
internal class PersistedOtherFactory(
        private val repository: OtherRepository,
        private val publisher: ApplicationEventPublisher)
    : OtherFactory {
    override fun all() = repository.findAll().map {
        toOther(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Other? {
        return repository.findByNaturalId(naturalId).map {
            toOther(it)
        }.orElse(null)
    }

    override fun createNew(naturalId: String) =
            PersistedOther(this, null, OtherRecord(naturalId))

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(naturalId)

    fun save(record: OtherRecord) =
            UpsertedRecordResult(record, repository.upsert(record))

    internal fun delete(record: OtherRecord) {
        repository.delete(record)
    }

    internal fun notifyChanged(
            before: OtherSnapshot?, after: OtherSnapshot?) =
            publisher.publishEvent(OtherChangedEvent(before, after))

    private fun toOther(record: OtherRecord) =
            PersistedOther(this, record.toSnapshot(), record)
}

internal open class PersistedOther(
        private val factory: PersistedOtherFactory,
        private var snapshot: OtherSnapshot?,
        private var record: OtherRecord?)
    : Other {
    override val naturalId: String
        get() = record().naturalId
    override val value: String?
        get() = record().value
    override val version: Int
        get() = record().version

    override val changed
        get() = snapshot != record().toSnapshot()

    /**
     * Notice that when **saving**, save the other _first_, so added
     * children have a valid FK reference.
     */
    @Transactional
    override fun save(): UpsertedDomainResult<OtherSnapshot, Other> {
        // Save ourselves first, so children have a valid other
        val before = snapshot
        val result =
                if (changed) factory.save(record())
                else UpsertedRecordResult(record(), false)
        record = result.record

        val after = record().toSnapshot()
        snapshot = after
        if (result.changed) // Trust the database
            factory.notifyChanged(before, after)
        return UpsertedDomainResult(this, result.changed)
    }

    /**
     * Notice that when **deleting**, save the other _last_, so that FK
     * references get cleared.
     */
    @Transactional
    override fun delete() {
        val before = snapshot
        factory.delete(record())

        val after = null as OtherSnapshot?
        record = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun <R> update(block: MutableOther.() -> R): R =
            PersistedMutableOther(record()).let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedOther
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() =
            Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"

    private fun record() =
            record ?: throw DomainException("Deleted: $this")
}

internal data class PersistedMutableOther(
        private val record: OtherRecord)
    : MutableOther,
        MutableOtherSimpleDetails by record

interface OtherRepository : CrudRepository<OtherRecord, Long> {
    @Query("""
        SELECT *
        FROM other
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<OtherRecord>

    @Query("""
        SELECT *
        FROM upsert_other(:naturalId, :value, :version)
        """)
    fun upsert(
            @Param("naturalId") naturalId: String,
            @Param("value") value: String?,
            @Param("version") version: Int)
            : Optional<OtherRecord>

    @JvmDefault
    fun upsert(entity: OtherRecord): Optional<OtherRecord> {
        val upserted = upsert(
                entity.naturalId,
                entity.value,
                entity.version)
        upserted.ifPresent {
            entity.upsertedWith(it)
        }
        return upserted
    }
}

@Table("other")
data class OtherRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var value: String?,
        override var version: Int)
    : MutableOtherSimpleDetails,
        UpsertableRecord<OtherRecord> {
    internal constructor(naturalId: String)
            : this(null, naturalId, null, 0)

    override fun upsertedWith(upserted: OtherRecord): OtherRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        value = upserted.value
        version = upserted.version
        return this
    }
}
