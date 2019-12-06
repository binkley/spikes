package x.domainpersistencemodeling.child

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES
import io.micronaut.data.repository.CrudRepository
import x.domainpersistencemodeling.workAroundArrayTypeForPostgresRead
import x.domainpersistencemodeling.workAroundArrayTypeForPostgresWrite
import java.time.OffsetDateTime
import java.util.Optional
import javax.inject.Singleton

@Singleton
internal class MicronautChildRepository(
    private val repository: InternalChildRepository
) : ChildRepository {
    override fun findAll(): Iterable<ChildRecord> =
        repository.findAll().map {
            it.fix()
        }

    override fun findByNaturalId(naturalId: String): Optional<ChildRecord> =
        repository.findByNaturalId(naturalId).map {
            it.fix()
        }

    override fun findByParentNaturalId(parentNaturalId: String)
            : Iterable<ChildRecord> =
        repository.findByParentNaturalId(parentNaturalId).map {
            it.fix()
        }

    override fun upsert(entity: ChildRecord): Optional<ChildRecord> =
        repository.upsert(
            entity.naturalId,
            entity.otherNaturalId,
            entity.parentNaturalId,
            entity.state,
            entity.at,
            entity.value,
            entity.defaultSideValues.workAroundArrayTypeForPostgresWrite(),
            entity.sideValues.workAroundArrayTypeForPostgresWrite(),
            entity.version
        ).map {
            it.fix()
            entity.upsertedWith(it)
        }

    override fun delete(entity: ChildRecord) {
        repository.delete(entity)
    }

    private fun ChildRecord.fix(): ChildRecord {
        defaultSideValues =
            defaultSideValues.workAroundArrayTypeForPostgresRead()
        sideValues = sideValues.workAroundArrayTypeForPostgresRead()
        return this
    }
}

@JdbcRepository(dialect = POSTGRES)
interface InternalChildRepository : CrudRepository<ChildRecord, Long> {
    @Query(
        """
        SELECT *
        FROM child
        WHERE natural_id = :naturalId
        """
    )
    fun findByNaturalId(naturalId: String)
            : Optional<ChildRecord>

    @Query(
        """
        SELECT *
        FROM child
        WHERE parent_natural_id = :parentNaturalId
        """
    )
    fun findByParentNaturalId(parentNaturalId: String)
            : Iterable<ChildRecord>

    @Query(
        """
        SELECT *
        FROM upsert_child(:naturalId, :otherNaturalId, :parentNaturalId,
        :state, :at, :value, :defaultSideValues, :sideValues, :version)
        """
    )
    fun upsert(
        naturalId: String,
        otherNaturalId: String?,
        parentNaturalId: String?,
        state: String,
        at: OffsetDateTime, // UTC
        value: String?,
        defaultSideValues: String,
        sideValues: String,
        version: Int
    )
            : Optional<ChildRecord>
}
