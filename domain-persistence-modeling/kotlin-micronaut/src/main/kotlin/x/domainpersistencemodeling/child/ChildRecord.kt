package x.domainpersistencemodeling.child

import io.micronaut.core.annotation.Introspected
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.UpsertableRecord
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.TreeSet
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Introspected
@Table(name = "child")
data class ChildRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var otherNaturalId: String?,
        override var parentNaturalId: String?,
        override var state: String,
        override var at: OffsetDateTime, // UTC
        override var value: String?,
        override var sideValues: MutableSet<String>,
        override var defaultSideValues: MutableSet<String>,
        override var version: Int)
    : MutableChildSimpleDetails,
        UpsertableRecord<ChildRecord> {
    internal constructor(naturalId: String)
            : this(null, naturalId, null, null, ENABLED.name,
            OffsetDateTime.ofInstant(
                    Instant.EPOCH,
                    ZoneOffset.UTC), null,
            mutableSetOf(),
            mutableSetOf(), 0)

    override fun upsertedWith(upserted: ChildRecord): ChildRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        otherNaturalId = upserted.otherNaturalId
        parentNaturalId = upserted.parentNaturalId
        state = upserted.state
        at = upserted.at
        value = upserted.value
        sideValues = TreeSet(upserted.sideValues)
        defaultSideValues = TreeSet(
                upserted.defaultSideValues)
        version = upserted.version
        return this
    }
}
