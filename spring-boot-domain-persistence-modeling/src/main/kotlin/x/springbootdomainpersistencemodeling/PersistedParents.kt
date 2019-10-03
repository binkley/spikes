package x.springbootdomainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.Instant.EPOCH
import java.util.*
import kotlin.reflect.KMutableProperty0

@Component
class PersistedParentFactory(
        private val repository: ParentRepository,
        private val publisher: ApplicationEventPublisher)
    : ParentFactory {
    override fun all() =
            repository.findAll().map {
                PersistedParent(it.asResource(), it, this)
            }.asSequence()

    override fun byNaturalId(naturalId: String) =
            repository.findByNaturalId(naturalId)?.let {
                PersistedParent(it.asResource(), it, this)
            }

    override fun new(resource: ParentResource) =
            PersistedParent(null, ParentRecord(resource), this)

    internal fun save(record: ParentRecord) = repository.save(record)

    internal fun delete(record: ParentRecord) = repository.delete(record)

    internal fun notifyChanged(
            before: ParentResource?, after: ParentResource?) =
            notifyIfChanged(before, after, publisher, ::ParentChangedEvent)

    internal fun idOf(resource: ParentResource) =
            repository.findByNaturalId(resource.naturalId)?.id

    internal fun resourceOf(id: Long): ParentResource? =
            repository.findById(id).orElse(null)?.asResource()
}

class PersistedParent internal constructor(
        private var snapshot: ParentResource?,
        private val record: ParentRecord,
        private val factory: PersistedParentFactory)
    : Parent {
    override val naturalId: String
        get() = record.naturalId
    override val value: String?
        get() = record.value
    override val version: Int
        get() = record.version

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedParent
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun update(block: MutableParent.() -> Unit) = apply {
        PersistedMutableParent(::snapshot, record, factory).block()
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

class PersistedMutableParent internal constructor(
        private val snapshot: KMutableProperty0<ParentResource?>,
        private val record: ParentRecord,
        private val factory: PersistedParentFactory)
    : MutableParent,
        MutableParentDetails by record {
    override fun save() = apply {
        factory.save(record)
        val after = record.asResource()
        factory.notifyChanged(snapshot.get(), after)
        snapshot.set(after)
    }

    override fun delete() {
        factory.delete(record)
        factory.notifyChanged(snapshot.get(), null)
        snapshot.set(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableParent
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

interface ParentRepository : CrudRepository<ParentRecord, Long> {
    @Query("SELECT * FROM parent WHERE natural_id = :naturalId")
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : ParentRecord?
}

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
