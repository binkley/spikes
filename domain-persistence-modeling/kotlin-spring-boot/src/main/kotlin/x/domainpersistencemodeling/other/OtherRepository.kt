package x.domainpersistencemodeling.other

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.Optional

interface OtherRepository : CrudRepository<OtherRecord, Long> {
    @Query(
        """
        SELECT *
        FROM other
        WHERE natural_id = :naturalId
        """
    )
    fun findByNaturalId(@Param("naturalId") naturalId: String)
            : Optional<OtherRecord>

    @Query(
        """
        SELECT *
        FROM upsert_other(:naturalId, :value, :version)
        """
    )
    fun upsert(
        @Param("naturalId") naturalId: String,
        @Param("value") value: String?,
        @Param("version") version: Int
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
