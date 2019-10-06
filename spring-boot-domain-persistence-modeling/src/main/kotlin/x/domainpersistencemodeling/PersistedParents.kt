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
import java.util.*

@Component
internal class PersistedParentFactory(
        private val repository: ParentRepository,
        private val publisher: ApplicationEventPublisher)
    : ParentFactory {
    override fun all(): Sequence<Parent> =
            repository.findAll().map {
                PersistedParent(it.asResource(), it, this)
            }.asSequence()

    override fun findExisting(naturalId: String): Parent? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                PersistedParent(it.asResource(), it, this)
            }

    override fun createNew(resource: ParentResource): Parent =
            PersistedParent(null, ParentRecord(resource), this)

    override fun findExistingOrCreateNew(naturalId: String): Parent =
            findExisting(naturalId) ?: createNew(
                    ParentResource(naturalId, null, 0))

    // TODO: Refetch to see changes in audit columns
    internal fun save(record: ParentRecord) =
            repository.findByNaturalId(
                    repository.save(record).naturalId).get()

    internal fun delete(record: ParentRecord) = repository.delete(record)

    internal fun notifyChanged(
            before: ParentResource?, after: ParentResource?) =
            notifyIfChanged(before, after, publisher, ::ParentChangedEvent)

    internal fun idOf(naturalId: String) =
            repository.findByNaturalId(naturalId).orElse(null)?.id

    internal fun resourceOf(id: Long) =
            repository.findById(id).orElse(null)?.asResource()
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
        val after = record!!.asResource()
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

    override fun toResource() = ParentResource(naturalId, value, version)

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
    internal constructor(resource: ParentResource) : this(
            null,
            resource.naturalId,
            resource.value,
            0,
            EPOCH,
            EPOCH)

    internal fun asResource() = ParentResource(naturalId, value, version)
}
