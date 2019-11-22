package x.domainpersistencemodeling.parent

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES
import io.micronaut.data.repository.CrudRepository
import x.domainpersistencemodeling.workAroundArrayTypeForPostgres
import java.util.Optional

@JdbcRepository(dialect = POSTGRES)
interface ParentRepository : CrudRepository<ParentRecord, Long> {
    @Query("""
        SELECT *
        FROM parent
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(naturalId: String)
            : Optional<ParentRecord>

    @Query("""
        SELECT *
        FROM upsert_parent(:naturalId, :otherNaturalId, :state, :value, :sideValues, :version)
        """)
    fun upsert(
            naturalId: String,
            otherNaturalId: String?,
            state: String,
            value: String?,
            sideValues: String,
            version: Int)
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