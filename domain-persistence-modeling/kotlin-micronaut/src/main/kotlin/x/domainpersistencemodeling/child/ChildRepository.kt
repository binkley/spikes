package x.domainpersistencemodeling.child

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES
import io.micronaut.data.repository.CrudRepository
import x.domainpersistencemodeling.workAroundArrayTypeForPostgres
import java.time.OffsetDateTime
import java.util.Optional

@JdbcRepository(dialect = POSTGRES)
interface ChildRepository : CrudRepository<ChildRecord, Long> {
    @Query("""
        SELECT *
        FROM child
        WHERE natural_id = :naturalId
        """)
    fun findByNaturalId(naturalId: String)
            : Optional<ChildRecord>

    @Query("""
        SELECT *
        FROM child
        WHERE parent_natural_id = :parentNaturalId
        """)
    fun findByParentNaturalId(parentNaturalId: String)
            : Iterable<ChildRecord>

    @Query("""
        SELECT *
        FROM upsert_child(:naturalId, :otherNaturalId, :parentNaturalId,
        :state, :at, :value, :sideValues, :defaultSideValues, :version)
        """)
    fun upsert(
            naturalId: String,
            otherNaturalId: String?,
            parentNaturalId: String?,
            state: String,
            at: OffsetDateTime, // UTC
            value: String?,
            sideValues: String,
            defaultSideValues: String,
            version: Int)
            : Optional<ChildRecord>
}

fun ChildRepository.upsert(entity: ChildRecord): Optional<ChildRecord> {
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
