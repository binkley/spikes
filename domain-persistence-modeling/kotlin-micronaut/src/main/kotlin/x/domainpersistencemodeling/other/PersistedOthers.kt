package x.domainpersistencemodeling.other

import io.micronaut.context.event.ApplicationEventPublisher
import x.domainpersistencemodeling.*
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import javax.inject.Singleton

@Singleton
internal class PersistedOtherFactory(
    private val repository: OtherRepository,
    private val publisher: ApplicationEventPublisher
) : OtherFactory,
    PersistedFactory<OtherSnapshot, OtherRecord, PersistedOtherDependentDetails> {
    override fun all() = repository.findAll().map {
        toDomain(it)
    }.asSequence()

    override fun findExisting(naturalId: String): Other? {
        return repository.findByNaturalId(naturalId).map {
            toDomain(it)
        }.orElse(null)
    }

    override fun createNew(naturalId: String) =
        PersistedOther(
            PersistedDomain(
                this,
                null,
                RecordHolder(OtherRecord(naturalId)),
                PersistedOtherDependentDetails(),
                ::PersistedOther
            )
        )

    override fun findExistingOrCreateNew(naturalId: String) =
        findExisting(naturalId) ?: createNew(naturalId)

    override fun findAssignedTo(parentOrChildNaturalId: String): Other? =
        repository.findByParentOrChildNaturalId(parentOrChildNaturalId).map {
            toDomain(it)
        }.orElse(null)

    override fun save(record: OtherRecord) =
        UpsertedRecordResult(record, repository.upsert(record))

    override fun delete(record: OtherRecord) {
        repository.delete(record)
    }

    override fun refreshPersistence(naturalId: String): OtherRecord =
        repository.findByNaturalId(naturalId).orElseThrow()

    override fun notifyChanged(
        before: OtherSnapshot?, after: OtherSnapshot?
    ) =
        publisher.publishEvent(OtherChangedEvent(before, after))

    override fun toSnapshot(
        record: OtherRecord,
        dependent: PersistedOtherDependentDetails
    ) =
        OtherSnapshot(record.naturalId, record.value, record.version)

    private fun toDomain(record: OtherRecord): PersistedOther {
        val dependent = PersistedOtherDependentDetails()
        return PersistedOther(
            PersistedDomain(
                this,
                toSnapshot(record, dependent),
                RecordHolder(record),
                dependent,
                ::PersistedOther
            )
        )
    }
}

internal data class PersistedOtherDependentDetails(
    private val saveMutated: Boolean = false
) : OtherDependentDetails,
    PersistedDependentDetails<OtherRecord> {
    override fun saveMutated() = saveMutated
}

internal class PersistedOther(
    private val persisted: PersistedDomain<OtherSnapshot, OtherRecord, PersistedOtherDependentDetails, PersistedOtherFactory, Other, MutableOther>
) : Other,
    PersistableDomain<OtherSnapshot, Other> by persisted {
    override val value: String?
        get() = persisted.record.value

    override fun <R> update(block: MutableOther.() -> R): R =
        PersistedMutableOther(persisted.record).let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersistedOther
        return persisted == other.persisted
    }

    override fun hashCode() = persisted.hashCode()

    override fun toString() = "${super.toString()}$persisted"
}

internal data class PersistedMutableOther(
    private val record: OtherRecord
) : MutableOther,
    MutableOtherSimpleDetails by record
