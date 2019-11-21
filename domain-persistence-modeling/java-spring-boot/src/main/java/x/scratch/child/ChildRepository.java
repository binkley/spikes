package x.scratch.child;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

import static x.scratch.Persistence.workAroundArrayTypeForPostgres;

@Repository
public interface ChildRepository
        extends CrudRepository<ChildRecord, Long> {
    @Query("SELECT * FROM child WHERE natural_id = :naturalId")
    Optional<ChildRecord> findByNaturalId(
            @Param("naturalId") String naturalId);

    @Query("SELECT * FROM child WHERE parent_natural_id = :parentNaturalId")
    Stream<ChildRecord> findByParentNaturalId(
            @Param("parentNaturalId") String parentNaturalId);

    @Query("SELECT * FROM upsert_child(:naturalId, :parentNaturalId,"
            + " :value, :defaultSideValues, :sideValues, :version)")
    Optional<ChildRecord> upsert(
            @Param("naturalId") final String naturalId,
            @Param("parentNaturalId") final String parentNaturalId,
            @Param("value") final String value,
            @Param("defaultSideValues") final String defaultSideValues,
            @Param("sideValues") final String sideValues,
            @Param("version") final Integer version);

    default Optional<ChildRecord> upsert(final ChildRecord entity) {
        final var upserted = upsert(entity.getNaturalId(),
                entity.getParentNaturalId(),
                entity.getValue(),
                workAroundArrayTypeForPostgres(entity.getDefaultSideValues()),
                workAroundArrayTypeForPostgres(entity.getSideValues()),
                entity.getVersion());
        upserted.ifPresent(entity::upsertedWith);
        return upserted;
    }
}
