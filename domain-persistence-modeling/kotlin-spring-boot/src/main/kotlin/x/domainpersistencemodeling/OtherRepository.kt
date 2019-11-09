package x.domainpersistencemodeling

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import x.domainpersistencemodeling.OtherRepository.OtherRecord
import java.util.Optional

interface OtherRepository : CrudRepository<OtherRecord, Long> {
    @Query("""
        SELECT *
        FROM other
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<OtherRecord>

    @Query("""
        SELECT *
        FROM upsert_other(:naturalId, :value, :version)
        """)
    fun upsert(
            @Param("naturalId") naturalId: String,
            @Param("value") value: String?,
            @Param("version") version: Int)
            : Optional<OtherRecord>

    @JvmDefault
    fun upsert(entity: OtherRecord): Optional<OtherRecord> {
        val upserted = upsert(
                entity.naturalId,
                entity.value,
                entity.version)
        upserted.ifPresent {
            entity.upsertedWith(it)
        }
        return upserted
    }

    @Table("other")
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
}
