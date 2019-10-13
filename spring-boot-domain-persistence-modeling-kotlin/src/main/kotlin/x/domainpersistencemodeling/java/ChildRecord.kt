package x.domainpersistencemodeling.java

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.TreeSet

@Table("child")
data class ChildRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var parentNaturalId: String?,
        override var value: String?,
        override var subchildren: Set<String>,
        override var version: Int
) : ChildDetails,
        UpsertableRecord<ChildRecord> {
    override fun updateWith(upserted: ChildRecord): ChildRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        parentNaturalId = upserted.parentNaturalId
        value = upserted.value
        subchildren = TreeSet(upserted.subchildren)
        version = upserted.version
        return this
    }

    companion object {
        internal fun createRecordFor(naturalId: String): ChildRecord {
            return ChildRecord(null, naturalId, null, null, TreeSet(),
                    0)
        }
    }
}
