package x.domainpersistencemodeling.java

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("parent")
data class ParentRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var value: String?,
        override var version: Int) : ParentDetails,
        UpsertableRecord<ParentRecord> {
    override fun updateWith(upserted: ParentRecord): ParentRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        value = upserted.value
        version = upserted.version
        return this
    }

    companion object {
        internal fun createRecordFor(naturalId: String): ParentRecord {
            return ParentRecord(null, naturalId, null, 0)
        }
    }
}
