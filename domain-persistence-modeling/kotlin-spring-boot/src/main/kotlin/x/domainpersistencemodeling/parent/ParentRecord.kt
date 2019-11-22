package x.domainpersistencemodeling.parent

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.UpsertableRecord
import java.util.TreeSet

@Table("parent")
data class ParentRecord(
        @Id var id: Long?,
        override var naturalId: String,
        override var otherNaturalId: String?,
        override var state: String,
        override var value: String?,
        override var sideValues: MutableSet<String>,
        override var version: Int)
    : MutableParentSimpleDetails,
        UpsertableRecord<ParentRecord> {
    internal constructor(naturalId: String)
            : this(null, naturalId, null, ENABLED.name, null,
            mutableSetOf(),
            0)

    override fun upsertedWith(upserted: ParentRecord): ParentRecord {
        id = upserted.id
        naturalId = upserted.naturalId
        otherNaturalId = upserted.otherNaturalId
        state = upserted.state
        value = upserted.value
        sideValues = TreeSet(upserted.sideValues)
        version = upserted.version
        return this
    }
}
