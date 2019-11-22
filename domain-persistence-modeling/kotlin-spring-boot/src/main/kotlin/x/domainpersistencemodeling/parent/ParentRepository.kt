package x.domainpersistencemodeling.parent

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import x.domainpersistencemodeling.workAroundArrayTypeForPostgres
import java.util.Optional

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
}

fun ParentRepository.upsert(entity: ParentRecord): Optional<ParentRecord> {
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
