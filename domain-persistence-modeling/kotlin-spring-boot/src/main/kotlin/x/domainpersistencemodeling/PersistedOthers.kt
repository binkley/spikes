package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import x.domainpersistencemodeling.OtherRepository.OtherRecord
import x.domainpersistencemodeling.PersistableDomain.UpsertedDomainResult
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects

@Component
internal class PersistedOtherFactory(
        private val repository: OtherRepository,
        private val publisher: ApplicationEventPublisher)
    : OtherFactory {
    override fun all() = repository.findAll().map {
        toOther(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Other? {
        return repository.findByNaturalId(naturalId).map {
            toOther(it)
        }.orElse(null)
    }

    override fun createNew(naturalId: String) =
            PersistedOther(this, null, OtherRecord(naturalId),
                    PersistedOtherComputedDetails())

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(naturalId)

    fun save(record: OtherRecord) =
            UpsertedRecordResult(record, repository.upsert(record))

    internal fun delete(record: OtherRecord) {
        repository.delete(record)
    }

    internal fun refreshRecord(naturalId: String) =
            repository.findByNaturalId(naturalId).orElseThrow()

    internal fun notifyChanged(
            before: OtherSnapshot?, after: OtherSnapshot?) =
            publisher.publishEvent(OtherChangedEvent(before, after))

    private fun toOther(record: OtherRecord): PersistedOther {
        val computed = PersistedOtherComputedDetails()
        return PersistedOther(this, record.toSnapshot(computed), record,
                computed)
    }
}

internal class PersistedOtherComputedDetails : OtherComputedDetails {
    internal fun saveMutated() = false
}

internal open class PersistedOther(
        private val factory: PersistedOtherFactory,
        private var snapshot: OtherSnapshot?,
        private var record: OtherRecord?,
        private val computed: PersistedOtherComputedDetails)
    : Other {
    override val naturalId: String
        get() = record().naturalId
    override val value: String?
        get() = record().value
    override val version: Int
        get() = record().version

    override val changed
        get() = snapshot != record().toSnapshot(computed)

    /**
     * Notice that when **saving**, save the other _first_, so added
     * children have a valid FK reference.
     */
    @Transactional
    override fun save(): UpsertedDomainResult<OtherSnapshot, Other> {
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

        val after = record().toSnapshot(computed)
        snapshot = after
        if (result.changed) // Trust the database
            factory.notifyChanged(before, after)
        return UpsertedDomainResult(this, result.changed)
    }

    /**
     * Notice that when **deleting**, save the other _last_, so that FK
     * references get cleared.
     */
    @Transactional
    override fun delete() {
        val before = snapshot
        computed.saveMutated()
        factory.delete(record())

        val after = null as OtherSnapshot?
        record = null
        snapshot = after
        factory.notifyChanged(before, after)
    }

    override fun <R> update(block: MutableOther.() -> R): R =
            PersistedMutableOther(record()).let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedOther
        return snapshot == other.snapshot
                && record == other.record
    }

    override fun hashCode() =
            Objects.hash(snapshot, record)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, record=$record}"

    private fun record() =
            record ?: throw DomainException("Deleted: $this")
}

internal data class PersistedMutableOther(
        private val record: OtherRecord)
    : MutableOther,
        MutableOtherSimpleDetails by record
