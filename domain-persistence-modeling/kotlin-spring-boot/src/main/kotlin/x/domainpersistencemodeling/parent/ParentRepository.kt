package x.domainpersistencemodeling.parent

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import x.domainpersistencemodeling.KnownState.ENABLED
import x.domainpersistencemodeling.UpsertableRecord
import x.domainpersistencemodeling.parent.ParentRepository.ParentRecord
import x.domainpersistencemodeling.workAroundArrayTypeForPostgres
import java.util.Optional
import java.util.TreeSet

interface ParentRepository : CrudRepository<ParentRecord, Long> {
    @Query("""
        SELECT *
        FROM parent
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<ParentRecord>

    @Query("""
        SELECT *
        FROM upsert_parent(:naturalId, :otherNaturalId, :state, :value, :sideValues, :version)
        """)
    fun upsert(
            @Param("naturalId") naturalId: String,
            @Param("otherNaturalId") otherNaturalId: String?,
            @Param("state") state: String,
            @Param("value") value: String?,
            @Param("sideValues") sideValues: String,
            @Param("version") version: Int)
            : Optional<ParentRecord>

    @JvmDefault
    fun upsert(entity: ParentRecord): Optional<ParentRecord> {
        val upserted = upsert(
                entity.naturalId,
                entity.otherNaturalId,
                entity.state,
                entity.value,
                entity.sideValues.workAroundArrayTypeForPostgres(),
                entity.version)
        upserted.ifPresent {
            entity.upsertedWith(it)
        }
        return upserted
    }

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
}
