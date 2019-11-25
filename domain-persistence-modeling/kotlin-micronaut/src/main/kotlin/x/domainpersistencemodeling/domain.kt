package x.domainpersistencemodeling

import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects.hash

internal interface PersistedFactory<Snapshot,
        Record : UpsertableRecord<Record>,
        Dependent : PersistedDependentDetails> {
    fun save(record: Record): UpsertedRecordResult<Record>
    fun delete(record: Record)
    fun refreshRecord(naturalId: String): Record
    fun notifyChanged(before: Snapshot?, after: Snapshot?)

    fun toSnapshot(record: Record, dependent: Dependent): Snapshot
}

internal interface PersistedDependentDetails {
    fun saveMutated(): Boolean
}

internal class PersistedDomain<Snapshot,
        Record : UpsertableRecord<Record>,
        Dependent : PersistedDependentDetails,
        Factory : PersistedFactory<Snapshot, Record, Dependent>,
        Domain : PersistableDomain<Snapshot, Domain>,
        Mutable>(
    private val factory: Factory,
    private var snapshot: Snapshot?,
    private var currentRecord: Record?,
    internal val dependent: Dependent,
    private val toDomain: (
        PersistedDomain
        <Snapshot, Record, Dependent, Factory, Domain, Mutable>
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
        get() = currentRecord ?: throw DomainException("Deleted: $this")

    /**
     * Notice that when **saving**, save the other _first_, so added
     * children have a valid FK reference.
     */
    @Suppress("UNCHECKED_CAST")
    override fun save(): UpsertedDomainResult<Snapshot, Domain> {
        // Save ourselves first, so children have a valid parent
        val before = snapshot
        var result =
            if (changed) factory.save(record)
            else UpsertedRecordResult(record, false)
        currentRecord = result.record

        if (dependent.saveMutated()) {
            // Refresh the version
            currentRecord = factory.refreshRecord(naturalId)
            result = UpsertedRecordResult(record, true)
        }

        val after = factory.toSnapshot(record, dependent)
        snapshot = after
        if (result.changed) // Trust the database
            factory.notifyChanged(before, after)
        return UpsertedDomainResult(toDomain(this), result.changed)
    }

    /**
     * Notice that when **deleting**, save the other _last_, so that FK
     * references get cleared.
     */
    override fun delete() {
        val before = snapshot
        dependent.saveMutated()
        factory.delete(record)

        val after = null as Snapshot?
        currentRecord = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedDomain<*, *, *, *, *, *>
        return snapshot == other.snapshot
                && currentRecord == other.currentRecord
                && dependent == other.dependent
    }

    override fun hashCode() = hash(snapshot, currentRecord, dependent)

    override fun toString() =
        "{snapshot=${snapshot}, record=${currentRecord}, dependent=${dependent}}"
}
