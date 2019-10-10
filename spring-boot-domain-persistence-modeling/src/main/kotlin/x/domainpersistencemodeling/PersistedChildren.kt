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
import java.util.TreeSet

@Component
internal open class PersistedChildFactory(
        private val repository: ChildRepository,
        private val publisher: ApplicationEventPublisher)
    : ChildFactory {
    override fun all(): Sequence<Child> = repository.findAll().map {
        toChild(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Child? =
            repository.findByNaturalId(naturalId).orElse(null)?.let {
                toChild(it)
            }

    override fun createNew(naturalId: String): Child =
            PersistedChild(null, ChildRecord(naturalId, null), this)

    override fun findExistingOrCreateNew(
            naturalId: String): Child =
            findExisting(naturalId) ?: createNew(naturalId)

    override fun findOwned(parentNaturalId: String) =
            repository.findByParentNaturalId(parentNaturalId).map {
                toChild(it)
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
            notifyIfChanged(before, after, publisher, ::ChildChangedEvent)

    private fun toChild(record: ChildRecord) =
            PersistedChild(toResource(record), record, this)

    private fun toResource(record: ChildRecord) = ChildResource(
            record.naturalId,
            record.parentNaturalId,
            record.value,
            record.subchildren,
            record.version)
}

internal class PersistedChild internal constructor(
        private var snapshot: ChildResource?,
        private var record: ChildRecord?,
        private val factory: PersistedChildFactory)
    : Child {
    override val naturalId: String
        get() = record!!.naturalId
    override val parentNaturalId: String?
        get() = record!!.parentNaturalId
    override val value: String?
        get() = record!!.value
    override val subchildren: Set<String> // Sorted
        get() = TreeSet(record!!.subchildren)
    override val version: Int
        get() = record!!.version
    override val existing: Boolean
        get() = 0 < version

    override fun update(block: MutableChild.() -> Unit) = apply {
        val mutable = PersistedMutableChild(record!!)
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
        private val record: ChildRecord)
    : MutableChild,
        MutableChildDetails by record {
    override var subchildren =
            TrackedSortedSet(record.subchildren,
                    ::resetSubchildrenToPreserveSorting,
                    ::resetSubchildrenToPreserveSorting)

    override fun assignTo(parent: Parent) = run {
        record.parentNaturalId = parent.naturalId
    }

    override fun unassignFromAny() = run {
        record.parentNaturalId = null
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

interface ChildRepository : CrudRepository<ChildRecord, Long> {
    @Query("""
        SELECT * FROM child WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<ChildRecord>

    @Query("""
        SELECT * FROM child
        WHERE parent_id = (SELECT id 
        FROM parent 
        WHERE natural_id = :parentNaturalId)
        """)
    fun findByParentNaturalId(
            @Param("parentNaturalId") parentNaturalId: String)
            : Iterable<ChildRecord>
}

@Table("child")
data class ChildRecord(
        @Id val id: Long?,
        override val naturalId: String,
        override var parentNaturalId: String?,
        override var value: String?,
        override var subchildren: MutableSet<String>,
        override val version: Int,
        val createdAt: Instant,
        val updatedAt: Instant) :
        MutableChildDetails {
    internal constructor(naturalId: String, parentNaturalId: String?)
            : this(null, naturalId, parentNaturalId, null, mutableSetOf(),
            0, EPOCH, EPOCH)
}
