package x.domainpersistencemodeling.other

import io.micronaut.core.annotation.Introspected
import x.domainpersistencemodeling.UpsertableRecord
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Introspected
@Table(name = "other")
data class OtherRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var value: String?,
        override var version: Int)
    : MutableOtherSimpleDetails,
        UpsertableRecord<OtherRecord> {
    internal constructor(naturalId: String)
            : this(null, naturalId, null, 0)

    override fun upsertedWith(upserted: OtherRecord): OtherRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        value = upserted.value
        version = upserted.version
        return this
    }
}
