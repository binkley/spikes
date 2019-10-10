package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.Instant.EPOCH
import java.util.Objects
import java.util.Optional

@Component
internal open class PersistedParentFactory(
        private val repository: ParentRepository,
        private val publisher: ApplicationEventPublisher)
    : ParentFactory {
    override fun all(): Sequence<Parent> = repository.findAll().map {
        toParent(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Parent? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                toParent(it)
            }

    override fun createNew(naturalId: String): Parent =
            PersistedParent(null, ParentRecord(naturalId), this)

    override fun findExistingOrCreateNew(
            naturalId: String): Parent =
            findExisting(naturalId) ?: createNew(naturalId)

    // TODO: Refetch to see changes in audit columns
    internal fun save(record: ParentRecord) =
            repository.findByNaturalId(
                    repository.save(record).naturalId).get()

    internal fun delete(record: ParentRecord) = repository.delete(record)

    internal fun notifyChanged(
            before: ParentResource?, after: ParentResource?) =
            notifyIfChanged(before, after, publisher, ::ParentChangedEvent)

    internal fun idFor(naturalId: String) =
            repository.findByNaturalId(naturalId)
                    .map(ParentRecord::id)
                    .get()

    internal fun naturalIdFor(id: Long) =
            repository.findById(id)
                    .map(ParentRecord::naturalId)
                    .get()

    internal fun toParent(record: ParentRecord) =
            PersistedParent(toResource(record), record, this)

    internal fun toResource(record: ParentRecord) =
            ParentResource(record.naturalId, record.value, record.version)
}

internal class PersistedParent internal constructor(
        private var snapshot: ParentResource?,
        private var record: ParentRecord?,
        private val factory: PersistedParentFactory)
    : Parent {
    override val naturalId: String
        get() = record!!.naturalId
    override val value: String?
        get() = record!!.value
    override val version: Int
        get() = record!!.version
    override val existing: Boolean
        get() = 0 < version

    override fun update(block: MutableParent.() -> Unit) = apply {
        val mutable = PersistedMutableParent(record!!)
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

    override fun toResource() = factory.toResource(record!!)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParent
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

internal class PersistedMutableParent internal constructor(
        private val record: ParentRecord)
    : MutableParent,
        MutableParentDetails by record {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableParent
        return record == other.record
    }

    override fun hashCode() = Objects.hash(record)

    override fun toString() = "${super.toString()}{record=$record}"
}

interface ParentRepository : CrudRepository<ParentRecord, Long> {
    @Query("SELECT * FROM parent WHERE natural_id = :naturalId")
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<ParentRecord>
}

@Table("parent")
data class ParentRecord(
        @Id val id: Long?,
        override val naturalId: String,
        override var value: String?,
        override val version: Int,
        val createdAt: Instant,
        val updatedAt: Instant)
    : MutableParentDetails {
    internal constructor(naturalId: String)
            : this(null, naturalId, null, 0, EPOCH, EPOCH)
}
