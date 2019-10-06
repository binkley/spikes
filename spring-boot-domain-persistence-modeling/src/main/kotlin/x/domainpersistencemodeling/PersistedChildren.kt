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
import kotlin.reflect.KMutableProperty0

@Component
class PersistedChildFactory(
        private val repository: ChildRepository,
        private val parentFactory: PersistedParentFactory,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory {
    override fun all() =
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

    // TODO: Refetch to see changes in audit columns
    internal fun save(record: ChildRecord) =
            repository.findByNaturalId(
                    repository.save(record).naturalId).get()

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
        private var record: ChildRecord,
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

    override fun update(block: MutableChild.() -> Unit) = let {
        val mutable = PersistedMutableChild(::snapshot, record, factory)
        mutable.block()
        mutable.asImmutable()
    }

    override fun updateAndSave(block: MutableChild.() -> Unit) =
            update(block)!!

    override fun delete() {
        update {
            delete()
        }
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
        private var record: ChildRecord?,
        private val factory: PersistedChildFactory)
    : MutableChild {
    override val naturalId: String
        get() = record!!.naturalId
    override var parentId: Long?
        get() = record!!.parentId
        set(parentId) {
            record!!.parentId = parentId
        }
    override var value: String?
        get() = record!!.value
        set(value) {
            record!!.value = value
        }
    override val version: Int
        get() = record!!.version

    override fun save() = apply {
        val before = snapshot.get()
        record = factory.save(record!!)
        val after = record!!.asResource(factory)
        snapshot.set(after)
        factory.notifyChanged(before, after)
    }

    override fun delete() {
        val before = snapshot.get()
        factory.delete(record!!)
        record = null
        val after = null
        snapshot.set(after)
        factory.notifyChanged(before, after)
    }

    internal fun asImmutable(): PersistedChild? {
        val record = this.record
        return if (null == record) null
        else PersistedChild(snapshot.get(), record, factory)
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

interface ChildRepository : CrudRepository<ChildRecord, Long> {
    @Query("SELECT * FROM child WHERE natural_id = :naturalId")
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<ChildRecord>
}

@Table("child")
data class ChildRecord(
        @Id val id: Long?,
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
