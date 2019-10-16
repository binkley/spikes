package x.scratch.child;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Repository
public interface ChildRepository
        extends CrudRepository<ChildRecord, Long> {
    private static String workAroundArrayType(
            @Nonnull final Set<String> subchildren) {
        // TODO: Workaround issue in Spring Data with passing sets for
        //  ARRAY types in a procedure
        return subchildren.stream()
                .collect(joining(",", "{", "}"));
    }

    @Query("SELECT * FROM child WHERE natural_id = :naturalId")
    Optional<ChildRecord> findByNaturalId(
            @Param("naturalId") String naturalId);

    @Query("SELECT * FROM child WHERE parent_natural_id = :parentNaturalId")
    Stream<ChildRecord> findByParentNaturalId(
            @Param("parentNaturalId") String parentNaturalId);

    @Query("SELECT * FROM upsert_child(:naturalId, :parentNaturalId,"
            + " :value, :subchildren, :version)")
    Optional<ChildRecord> upsert(
            @Param("naturalId") final String naturalId,
            @Param("parentNaturalId") final String parentNaturalId,
            @Param("value") final String value,
            @Param("subchildren") final String subchildren,
            @Param("version") final Integer version);

    default Optional<ChildRecord> upsert(final ChildRecord entity) {
        final var upserted = upsert(entity.getNaturalId(),
                entity.getParentNaturalId(),
                entity.getValue(),
                workAroundArrayType(entity.getSubchildren()),
                entity.getVersion());
        upserted.ifPresent(entity::upsertedWith);
        return upserted;
    }
}
