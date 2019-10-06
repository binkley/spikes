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
internal class PersistedChildFactory(
        private val repository: ChildRepository,
        private val parentFactory: PersistedParentFactory,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory {
    override fun all(): Sequence<Child> =
            repository.findAll().map {
                PersistedChild(it.asResource(this), it, this)
            }.asSequence()

    override fun findExisting(naturalId: String): Child? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                PersistedChild(it.asResource(this), it, this)
            }

    override fun createNew(naturalId: String): Child =
            PersistedChild(null, ChildRecord(
                    ChildResource(naturalId, null, null, 0), this),
                    this)

    override fun findExistingOrCreateNew(naturalId: String): Child =
            findExisting(naturalId) ?: createNew(naturalId)

    // TODO: Refetch to see changes in audit columns
    internal fun save(record: ChildRecord) =
            repository.findByNaturalId(
                    repository.save(record).naturalId).get()

    internal fun delete(record: ChildRecord) = repository.delete(record)

    internal fun notifyChanged(
            before: ChildResource?, after: ChildResource?) =
            notifyIfChanged(before, after, publisher, ::ChildChangedEvent)

    internal fun addTo(child: MutableChild, parent: ParentResource) =
            repository.updateParentId(child.naturalId, parent.naturalId)
                    .orElse(null)

    internal fun parentIdFor(resource: ChildResource) =
            resource.parent?.let {
                parentFactory.idOf(it)
            }

    internal fun parentResourceFor(parentId: Long?) =
            parentId?.let {
                parentFactory.resourceOf(parentId)
            }
}

internal class PersistedChild internal constructor(
        private var snapshot: ChildResource?,
        private var record: ChildRecord?,
        private val factory: PersistedChildFactory)
    : Child {
    override val naturalId: String
        get() = record!!.naturalId
    override val parentId: Long?
        get() = record!!.parentId
    override val value: String?
        get() = record!!.value
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
        val after = record!!.asResource(factory)
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

internal class PersistedMutableChild internal constructor(
        private val record: ChildRecord,
        private val factory: PersistedChildFactory)
    : MutableChild,
        MutableChildDetails by record {
    override fun addTo(parent: ParentResource) = apply {
        factory.addTo(this, parent)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedMutableChild
        return record == other.record
    }

    override fun hashCode() = Objects.hash(record)

    override fun toString() = "${super.toString()}{record=$record}"
}

interface ChildRepository : CrudRepository<ChildRecord, Long> {
    @Query("SELECT * FROM child WHERE natural_id = :naturalId")
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<ChildRecord>

    @Query("""
        UPDATE child
        SET parent_id = (SELECT id FROM parent WHERE natural_id = :parentNaturalId)
        WHERE natural_id = :naturalId
        RETURNING *
        """)
    fun updateParentId(
            @Param("naturalId") naturalId: String,
            @Param("parentNaturalId") parentNaturalId: String)
            : Optional<ChildRecord>
}

@Table("child")
data class ChildRecord(
        @Id val id: Long?,
        override val naturalId: String,
        override var parentId: Long?,
        override var value: String?,
        override val version: Int,
        val createdAt: Instant,
        val updatedAt: Instant) :
        MutableChildDetails {
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
