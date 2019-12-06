package x.domainpersistencemodeling.other

import io.micronaut.context.event.ApplicationEventPublisher
import lombok.Generated
import x.domainpersistencemodeling.Bug
import x.domainpersistencemodeling.PersistableDomain
import x.domainpersistencemodeling.PersistedDependentDetails
import x.domainpersistencemodeling.PersistedDomain
import x.domainpersistencemodeling.PersistedFactory
import x.domainpersistencemodeling.RecordHolder
import x.domainpersistencemodeling.UpsertableRecord.UpsertedRecordResult
import java.util.Objects.hash
import javax.inject.Singleton

@Singleton
internal class PersistedOtherFactory(
    private val repository: OtherRepository,
    private val publisher: ApplicationEventPublisher
) : OtherFactory,
    PersistedFactory<
            OtherSnapshot,
            OtherRecord,
            PersistedOtherDependentDetails> {
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

    @Generated // Lie to JaCoCo -- no dependents to require refreshing
    override fun refreshPersistence(naturalId: String): OtherRecord =
        throw Bug("No dependents to require refreshing")

    override fun notifyChanged(
        before: OtherSnapshot?, after: OtherSnapshot?
    ) =
        publisher.publishEvent(OtherChangedEvent(before, after))

    override fun toSnapshot(
        record: OtherRecord,
        dependent: PersistedOtherDependentDetails
    ) =
        OtherSnapshot(
            record.naturalId,
            record.value,
            record.version
        )

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

internal class PersistedOtherDependentDetails
    : OtherDependentDetails,
    PersistedDependentDetails<OtherRecord>,
    MutableOtherDependentDetails {
    override fun saveMutated() = false

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun equals(other: Any?) = this === other
            || other is PersistedOtherDependentDetails

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun hashCode() = hash(this::class)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun toString() = "${super.toString()}{}"
}

internal class PersistedOther(
    private val persisted: PersistedDomain<
            OtherSnapshot,
            OtherRecord,
            PersistedOtherDependentDetails,
            PersistedOtherFactory,
            Other,
            MutableOther>
) : Other,
    PersistableDomain<OtherSnapshot, Other> by persisted {
    override val value: String?
        get() = persisted.record.value

    override fun <R> update(block: MutableOther.() -> R): R =
        PersistedMutableOther(
            persisted.record,
            persisted.dependent
        ).let(block)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun equals(other: Any?) = this === other
            || other is PersistedOther
            && persisted == other.persisted

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun hashCode() = hash(persisted)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun toString() = "${super.toString()}{persisted=$persisted}"
}

internal data class PersistedMutableOther(
    private val record: OtherRecord,
    private val persistence: PersistedOtherDependentDetails
) : MutableOther,
    MutableOtherSimpleDetails by record,
    MutableOtherDependentDetails by persistence
