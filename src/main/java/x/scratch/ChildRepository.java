package x.scratch;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChildRepository extends CrudRepository<ChildRecord, Long> {
    @Query("SELECT * FROM child WHERE natural_id = :naturalId")
    Optional<ChildRecord> findByNaturalId(@Param("naturalId") String naturalId);

    @Query("SELECT * FROM upsert_child(:naturalId, :parentNaturalId, :value, :version)")
    ChildRecord upsert(
            @Param("naturalId") final String naturalId,
            @Param("parentNaturalId") final String parentNaturalId,
            @Param("value") final String value,
            @Param("version") final Integer version);

    default ChildRecord upsert(final ChildRecord entity) {
        return entity.updateWith(upsert(
                entity.getNaturalId(), entity.getParentNaturalId(), entity.getValue(),
                entity.getVersion()));
    }
}
