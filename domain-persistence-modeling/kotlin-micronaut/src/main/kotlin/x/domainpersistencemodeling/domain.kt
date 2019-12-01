package x.domainpersistencemodeling

import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects.hash

internal interface PersistedFactory<Snapshot,
        Record : UpsertableRecord<Record>,
        Dependent : PersistedDependentDetails<Record>> {
    fun save(record: Record): UpsertedRecordResult<Record>
    fun delete(record: Record)
    fun refreshPersistence(naturalId: String): Record
    fun notifyChanged(before: Snapshot?, after: Snapshot?)
    fun toSnapshot(record: Record, dependent: Dependent): Snapshot
}

internal data class RecordHolder<Record : UpsertableRecord<Record>>(
    var record: Record?
)

internal interface PersistedDependentDetails<
        Record : UpsertableRecord<Record>> {
    fun saveMutated(): Boolean
}

internal class PersistedDomain<Snapshot,
        Record : UpsertableRecord<Record>,
        Dependent : PersistedDependentDetails<Record>,
        Factory : PersistedFactory<Snapshot, Record, Dependent>,
        Domain : PersistableDomain<Snapshot, Domain>,
        Mutable>(
    private val factory: Factory,
    private var snapshot: Snapshot?,
    private val holder: RecordHolder<Record>,
    internal val dependent: Dependent,
    private val toDomain: (
        PersistedDomain<Snapshot, Record, Dependent, Factory, Domain, Mutable>
    ) -> Domain
) : PersistableDomain<Snapshot, Domain> {
    override val naturalId: String
        get() = record.naturalId
    override val version: Int
        get() = record.version
    override val changed
        get() = snapshot != factory.toSnapshot(record, dependent)

    /** Throws [DomainException] if the domain object has been deleted. */
    internal val record: Record
        get() = holder.record ?: throw DomainException("Deleted: $this")

    /**
     * Notice that when **saving**, save ourselves _first_, so added
     * children have a valid FK reference.
     */
    override fun save(): UpsertedDomainResult<Snapshot, Domain> {
        val before = snapshot
        // TODO: val revertRecord = computed from snapshot?

        var recordResult =
            if (changed) factory.save(record)
            else UpsertedRecordResult(record, false)
        // Refresh our version since children mutated in the DB
        if (dependent.saveMutated()) recordResult = UpsertedRecordResult(
            factory.refreshPersistence(naturalId), true
        )

        val after = factory.toSnapshot(recordResult.record, dependent)
        snapshot = after
        holder.record = recordResult.record

        if (after != before) try {
            factory.notifyChanged(before, after)
        } catch (e: DomainException) {
            snapshot = before
            // TODO: holder.record = revertRecord
            throw e
        }

        return UpsertedDomainResult(toDomain(this), recordResult.changed)
    }

    /**
     * Notice that when **deleting**, save ourselves _last_, so that FK
     * references by children are valid.
     */
    override fun delete() {
        val before = snapshot
        val revertRecord = record

        dependent.saveMutated()
        factory.delete(record)

        val after = null as Snapshot?
        snapshot = after
        holder.record = null

        try {
            factory.notifyChanged(before, after)
        } catch (e: DomainException) {
            snapshot = before
            holder.record = revertRecord
            throw e
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedDomain<*, *, *, *, *, *>
        return snapshot == other.snapshot
                && holder == other.holder
                && dependent == other.dependent
    }

    override fun hashCode() = hash(snapshot, holder, dependent)

    override fun toString() =
        "{snapshot=${snapshot}, holder=${holder}, dependent=${dependent}}"
}
