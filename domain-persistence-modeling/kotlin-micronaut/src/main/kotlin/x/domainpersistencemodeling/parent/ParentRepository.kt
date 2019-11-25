package x.domainpersistencemodeling.parent

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES
import io.micronaut.data.repository.CrudRepository
import x.domainpersistencemodeling.workAroundArrayTypeForPostgresRead
import x.domainpersistencemodeling.workAroundArrayTypeForPostgresWrite
import java.util.Optional
import javax.inject.Singleton

@Singleton
internal class ParentRepository(
    private val repository: InternalParentRepository
) {
    fun findAll(): Iterable<ParentRecord> =
        repository.findAll().map {
            it.fix()
        }

    fun findByNaturalId(naturalId: String): Optional<ParentRecord> =
        repository.findByNaturalId(naturalId).map {
            it.fix()
        }

    fun upsert(entity: ParentRecord) =
        repository.upsert(
            entity.naturalId,
            entity.otherNaturalId,
            entity.state,
            entity.value,
            entity.sideValues.workAroundArrayTypeForPostgresWrite(),
            entity.version
        ).map {
            it.fix()
            entity.upsertedWith(it)
        }

    fun delete(entity: ParentRecord) {
        repository.delete(entity)
    }

    private fun ParentRecord.fix(): ParentRecord {
        sideValues = sideValues.workAroundArrayTypeForPostgresRead()
        return this
    }
}

@JdbcRepository(dialect = POSTGRES)
interface InternalParentRepository : CrudRepository<ParentRecord, Long> {
    @Query(
        """
        SELECT *
        FROM parent
        WHERE natural_id = :naturalId
        """
    )
    fun findByNaturalId(naturalId: String)
            : Optional<ParentRecord>

    @Query(
        """
        SELECT *
        FROM upsert_parent(:naturalId, :otherNaturalId, :state, :value, :sideValues, :version)
        """
    )
    fun upsert(
        naturalId: String,
        otherNaturalId: String?,
        state: String,
        value: String?,
        sideValues: String,
        version: Int
    )
            : Optional<ParentRecord>
}
