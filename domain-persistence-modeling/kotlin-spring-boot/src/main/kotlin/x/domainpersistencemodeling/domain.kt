package x.domainpersistencemodeling

import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult

internal interface PersistedFactory<Snapshot,
        Record : UpsertableRecord<Record>,
        Computed : PersistedComputedDetails> {
    fun save(record: Record): UpsertedRecordResult<Record>
    fun delete(record: Record)
    fun refreshRecord(naturalId: String): Record
    fun notifyChanged(before: Snapshot?, after: Snapshot?)

    fun toSnapshot(record: Record, computed: Computed): Snapshot
}

internal interface PersistedComputedDetails {
    fun saveMutated(): Boolean
}

internal class PersistedDomain<Snapshot,
        Record : UpsertableRecord<Record>,
        Computed : PersistedComputedDetails,
        Factory : PersistedFactory<Snapshot, Record, Computed>,
        Domain,
        Mutable>(
        private val factory: Factory,
        var snapshot: Snapshot?,
        var record: Record?,
        private val computed: Computed,
        private val toDomain: (PersistedDomain
        <Snapshot, Record, Computed, Factory, Domain, Mutable>) -> Domain,
        private val toMutable: (Record) -> Mutable)
    : PersistableDomain<Snapshot, Domain>,
        ScopedMutable<Domain, Mutable>
        where Domain : ScopedMutable<Domain, Mutable>,
              Domain : PersistableDomain<Snapshot, Domain> {
    override val naturalId: String
        get() = record().naturalId
    override val version: Int
        get() = record().version
    override val changed
        get() = snapshot != factory.toSnapshot(record(), computed)

    fun record() =
            record ?: throw DomainException("Deleted: $this")

    /**
     * Notice that when **saving**, save the other _first_, so added
     * children have a valid FK reference.
     */
    @Suppress("UNCHECKED_CAST")
    override fun save(): UpsertedDomainResult<Snapshot, Domain> {
        // Save ourselves first, so children have a valid parent
        val before = snapshot
        var result =
                if (changed) factory.save(record())
                else UpsertedRecordResult(record(), false)
        record = result.record

        if (computed.saveMutated()) {
            // Refresh the version
            record = factory.refreshRecord(naturalId)
            result = UpsertedRecordResult(record(), true)
        }

        val after = factory.toSnapshot(record(), computed)
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
        computed.saveMutated()
        factory.delete(record())

        val after = null as Snapshot?
        record = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun <R> update(block: Mutable.() -> R): R =
            toMutable(record()).let(block)
}
