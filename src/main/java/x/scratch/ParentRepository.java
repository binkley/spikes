package x.scratch;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends CrudRepository<ParentRecord, Long> {
    @Query("SELECT * FROM parent WHERE natural_id = :naturalId")
    Optional<ParentRecord> findByNaturalId(@Param("naturalId") String naturalId);

    @Query("SELECT * FROM upsert_parent(:naturalId, :value, :version)")
    ParentRecord upsert(
            @Param("naturalId") final String naturalId,
            @Param("value") final String value,
            @Param("version") final Integer version);

    default ParentRecord upsert(final ParentRecord entity) {
        return entity.updateWith(upsert(
                entity.getNaturalId(), entity.getValue(), entity.getVersion()));
    }
}
