package x.scratch.child;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static java.util.stream.Collectors.joining;

@Repository
public interface ChildRepository
        extends CrudRepository<ChildRecord, Long> {
    @Query("SELECT * FROM child WHERE natural_id = :naturalId")
    Optional<ChildRecord> findByNaturalId(
            @Param("naturalId") String naturalId);

    @Query("SELECT * FROM upsert_child(:naturalId, :parentNaturalId,"
            + " :value, :subchildren, :version)")
    ChildRecord upsert(
            @Param("naturalId") final String naturalId,
            @Param("parentNaturalId") final String parentNaturalId,
            @Param("value") final String value,
            @Param("subchildren") final String subchildren,
            @Param("version") final Integer version);

    default ChildRecord upsert(final ChildRecord entity) {
        // TODO: Workaround issue in Spring Data with passing sets for
        //  ARRAY types in a procedure
        final var upserted = upsert(entity.getNaturalId(),
                entity.getParentNaturalId(),
                entity.getValue(),
                entity.getSubchildren().stream()
                        .collect(joining(",", "{", "}")),
                entity.getVersion());
        if (null != upserted) {
            entity.updateWith(upserted);
        }
        return upserted;
    }
}
