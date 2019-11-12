package x.domainpersistencemodeling

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import x.domainpersistencemodeling.OtherRepository.OtherRecord
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects

@Component
internal class PersistedOtherFactory(
        private val repository: OtherRepository,
        private val publisher: ApplicationEventPublisher)
    : OtherFactory,
        PersistedFactory<OtherSnapshot, OtherRecord, PersistedOtherComputedDetails> {
    override fun all() = repository.findAll().map {
        toDomain(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Other? {
        return repository.findByNaturalId(naturalId).map {
            toDomain(it)
        }.orElse(null)
    }

    override fun createNew(naturalId: String) =
            PersistedOther(PersistedDomain(
                    this, null as OtherSnapshot?, OtherRecord(naturalId),
                    PersistedOtherComputedDetails(),
                    ::PersistedOther,
                    ::PersistedMutableOther))

    override fun findExistingOrCreateNew(naturalId: String) =
            findExisting(naturalId) ?: createNew(naturalId)

    override fun save(record: OtherRecord) =
            UpsertedRecordResult(record, repository.upsert(record))

    override fun delete(record: OtherRecord) {
        repository.delete(record)
    }

    override fun refreshRecord(naturalId: String): OtherRecord =
            repository.findByNaturalId(naturalId).orElseThrow()

    override fun notifyChanged(
            before: OtherSnapshot?, after: OtherSnapshot?) =
            publisher.publishEvent(OtherChangedEvent(before, after))

    override fun toSnapshot(record: OtherRecord,
            computed: PersistedOtherComputedDetails) =
            OtherSnapshot(record.naturalId, record.value, record.version)

    private fun toDomain(record: OtherRecord): PersistedOther {
        val computed = PersistedOtherComputedDetails()
        return PersistedOther(PersistedDomain(
                this, toSnapshot(record, computed), record, computed,
                ::PersistedOther,
                ::PersistedMutableOther))
    }
}

internal class PersistedOtherComputedDetails
    : OtherComputedDetails,
        PersistedComputedDetails {
    override fun saveMutated() = false
}

internal open class PersistedOther(
        private val persisted: PersistedDomain<OtherSnapshot, OtherRecord, PersistedOtherComputedDetails, PersistedOtherFactory, Other, MutableOther>)
    : Other,
        PersistableDomain<OtherSnapshot, Other> by persisted,
        ScopedMutable<Other, MutableOther> by persisted {
    override val value: String?
        get() = persisted.record().value

    override fun <R> update(block: MutableOther.() -> R): R =
            PersistedMutableOther(persisted.record()).let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedOther
        return persisted.snapshot == other.persisted.snapshot
                && persisted.record == other.persisted.record
    }

    override fun hashCode() =
            Objects.hash(persisted.snapshot, persisted.record)

    override fun toString() =
            "${super.toString()}{snapshot=$persisted.snapshot, record=$persisted.record}"
}

internal data class PersistedMutableOther(
        private val record: OtherRecord)
    : MutableOther,
        MutableOtherSimpleDetails by record
