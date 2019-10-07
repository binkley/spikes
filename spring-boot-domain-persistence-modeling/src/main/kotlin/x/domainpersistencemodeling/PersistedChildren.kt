package x.domainpersistencemodeling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
        private val objectMapper: ObjectMapper,
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

    internal fun addTo(child: MutableChild, parent: ParentResource) =
            repository.updateParentId(
                    child.naturalId,
                    parent.naturalId)
                    .orElse(null)

    internal fun parentNaturalIdFor(parentId: Long) =
            parentFactory.naturalIdFor(parentId)

    internal fun fromJsonArray(json: String): List<String> =
            objectMapper.readValue(json)

    internal fun toJsonArray(items: List<String>): String =
            objectMapper.writeValueAsString(items)

    private fun forRecord(record: ChildRecord): PersistedChild {
        val parentId = record.parentId
        val resource = ChildResource(
                record.naturalId,
                parentId?.let {
                    parentNaturalIdFor(it)
                },
                record.value,
                fromJsonArray(record.subchildJson),
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
    override val subchildren: List<String>
        get() = factory.fromJsonArray(record!!.subchildJson)
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
    override var subchildren: MutableList<String>
        get() = factory.fromJsonArray(record.subchildJson).toMutableList()
        set(value) {
            // TODO: BORKEN -- list needs to save back each update
            record.subchildJson = factory.toJsonArray(value)
        }

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
        override var subchildJson: String,
        override val version: Int,
        val createdAt: Instant,
        val updatedAt: Instant) :
        MutableChildDetails {
    internal constructor(naturalId: String, parentId: Long?)
            : this(null, naturalId, parentId, null, "[]", 0, EPOCH, EPOCH)
}
