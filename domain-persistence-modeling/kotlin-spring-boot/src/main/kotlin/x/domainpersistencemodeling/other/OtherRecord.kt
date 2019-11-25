package x.domainpersistencemodeling.other

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import x.domainpersistencemodeling.UpsertableRecord

@Table("other")
data class OtherRecord(
    @Id var id: Long?,
    override var naturalId: String,
    override var value: String?,
    override var version: Int
) : MutableOtherSimpleDetails,
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
