package x.domainpersistencemodeling

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import x.domainpersistencemodeling.ChildRepository.ChildRecord
import x.domainpersistencemodeling.KnownState.ENABLED
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Optional
import java.util.TreeSet

@Repository
interface ChildRepository : CrudRepository<ChildRecord, Long> {
    @Query("""
        SELECT *
        FROM child
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<ChildRecord>

    @Query("""
        SELECT *
        FROM child
        WHERE parent_natural_id = :parentNaturalId
        """)
    fun findByParentNaturalId(
            @Param("parentNaturalId") parentNaturalId: String)
            : Iterable<ChildRecord>

    @Query("""
        SELECT *
        FROM upsert_child(:naturalId, :otherNaturalId, :parentNaturalId,
        :state, :at, :value, :sideValues, :defaultSideValues, :version)
        """)
    fun upsert(
            @Param("naturalId") naturalId: String,
            @Param("otherNaturalId") otherNaturalId: String?,
            @Param("parentNaturalId") parentNaturalId: String?,
            @Param("state") state: String,
            @Param("at") at: OffsetDateTime, // UTC
            @Param("value") value: String?,
            @Param("sideValues") sideValues: String,
            @Param("defaultSideValues") defaultSideValues: String,
            @Param("version") version: Int)
            : Optional<ChildRecord>

    @JvmDefault
    fun upsert(entity: ChildRecord): Optional<ChildRecord> {
        val upserted = upsert(entity.naturalId,
                entity.otherNaturalId,
                entity.parentNaturalId,
                entity.state,
                entity.at,
                entity.value,
                entity.sideValues.workAroundArrayTypeForPostgres(),
                entity.defaultSideValues.workAroundArrayTypeForPostgres(),
                entity.version)
        upserted.ifPresent {
            entity.upsertedWith(it)
        }
        return upserted
    }

    @Table("child")
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
                OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC), null,
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
            defaultSideValues = TreeSet(upserted.defaultSideValues)
            version = upserted.version
            return this
        }
    }
}
