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
        SELECT other.*
        FROM other
        JOIN parent ON other.natural_id = parent.other_natural_id
        WHERE parent.natural_id = :parentOrChildNaturalId
        UNION
        SELECT other.*
        FROM other
        JOIN child ON other.natural_id = child.other_natural_id
        WHERE child.natural_id = :parentOrChildNaturalId
        """
    )
    fun findByParentOrChildNaturalId(parentOrChildNaturalId: String)
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

fun OtherRepository.upsert(entity: OtherRecord): Optional<OtherRecord> =
    upsert(
        entity.naturalId,
        entity.value,
        entity.version
    ).map {
        entity.upsertedWith(it)
    }
