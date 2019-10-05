package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.core.annotation.Introspected
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
import kotlin.reflect.KMutableProperty0

@Singleton
class PersistedChildFactory(
        private val repository: ChildRepository,
        private val parentFactory: PersistedParentFactory,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory {
    override fun all(): Sequence<Child> =
            repository.findAll().map {
                PersistedChild(it.asResource(this), it, this)
            }.asSequence()

    override fun findExisting(naturalId: String) =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                PersistedChild(it.asResource(this), it, this)
            }

    override fun createNew(resource: ChildResource): Child =
            PersistedChild(null, ChildRecord(resource, this), this)

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(
                    ChildResource(naturalId, null, null, 0))

    internal fun save(record: ChildRecord) = repository.save(record)

    internal fun delete(record: ChildRecord) = repository.delete(record)

    internal fun notifyChanged(
            before: ChildResource?, after: ChildResource?) =
            notifyIfChanged(before, after, publisher, ::ChildChangedEvent)

    internal fun parentIdFor(resource: ChildResource) =
            resource.parent?.let {
                parentFactory.idOf(it)
            }

    internal fun parentResourceFor(parentId: Long?) =
            parentId?.let {
                parentFactory.resourceOf(parentId)
            }
}

class PersistedChild internal constructor(
        private var snapshot: ChildResource?,
        private val record: ChildRecord,
        private val factory: PersistedChildFactory)
    : Child {
    override val naturalId: String
        get() = record.naturalId
    override val parentId: Long?
        get() = record.parentId
    override val value: String?
        get() = record.value
    override val version: Int
        get() = record.version
    override val existing: Boolean
        get() = 0 < version

    override fun update(block: MutableChild.() -> Unit) = apply {
        PersistedMutableChild(::snapshot, record, factory).block()
    }

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
}

class PersistedMutableChild internal constructor(
        private val snapshot: KMutableProperty0<ChildResource?>,
        private val record: ChildRecord,
        private val factory: PersistedChildFactory)
    : MutableChild {
    override val naturalId: String
        get() = record.naturalId
    override var parentId: Long?
        get() = record.parentId
        set(parentId) {
            record.parentId = parentId
        }
    override var value: String?
        get() = record.value
        set(value) {
            record.value = value
        }
    override val version: Int
        get() = record.version

    override fun save() = apply {
        factory.save(record)
        val before = snapshot.get()
        val after = record.asResource(factory)
        snapshot.set(after)
        factory.notifyChanged(before, after)
    }

    override fun delete() {
        factory.delete(record)
        factory.notifyChanged(snapshot.get(), null)
        snapshot.set(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableChild
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() = Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"
}

@JdbcRepository(dialect = POSTGRES)
interface ChildRepository : CrudRepository<ChildRecord, Long> {
    // TODO: Can I return Kotlin `ChildRecord?`
    fun findByNaturalId(naturalId: String): Optional<ChildRecord>
}

@Introspected
@Table(name = "child")
data class ChildRecord(
        @Id @GeneratedValue val id: Long?,
        val naturalId: String,
        var parentId: Long?,
        var value: String?,
        val version: Int,
        val createdAt: Instant,
        val updatedAt: Instant) {
    internal constructor(resource: ChildResource,
            factory: PersistedChildFactory) : this(
            null,
            resource.naturalId,
            factory.parentIdFor(resource),
            resource.value,
            resource.version,
            EPOCH,
            EPOCH)

    internal fun asResource(factory: PersistedChildFactory) =
            ChildResource(
                    naturalId,
                    factory.parentResourceFor(parentId),
                    value,
                    version)
}
