package x.domainpersistencemodeling.other

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES
import io.micronaut.data.repository.CrudRepository
import java.util.Optional

@JdbcRepository(dialect = POSTGRES)
interface OtherRepository : CrudRepository<OtherRecord, Long> {
    @Query(
        """
        SELECT *
        FROM other
        WHERE natural_id = :naturalId
        """
    )
    fun findByNaturalId(naturalId: String)
            : Optional<OtherRecord>

    @Query(
        """
        SELECT *
        FROM upsert_other(:naturalId, :value, :version)
        """
    )
    fun upsert(
        naturalId: String,
        value: String?,
        version: Int
    )
            : Optional<OtherRecord>
}

fun OtherRepository.upsert(entity: OtherRecord): Optional<OtherRecord> {
    val upserted = upsert(
        entity.naturalId,
        entity.value,
        entity.version
    )
    upserted.ifPresent {
        entity.upsertedWith(it)
    }
    return upserted
}
