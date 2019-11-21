package x.scratch.child;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import x.scratch.UpsertableRecord;

import java.util.Set;
import java.util.TreeSet;

import static lombok.AccessLevel.PACKAGE;

@AllArgsConstructor(access = PACKAGE)
@Builder(toBuilder = true)
@Data
@Table("child")
public class ChildRecord
        implements ChildSimpleDetails,
        UpsertableRecord<ChildRecord> {
    @Id
    private Long id;
    private @NonNull String naturalId;
    private String parentNaturalId;
    private String value;
    @Builder.Default
    private @NonNull Set<String> defaultSideValues = new TreeSet<>();
    @Builder.Default
    private @NonNull Set<String> sideValues = new TreeSet<>();
    private int version;

    static ChildRecord createRecordFor(final String naturalId) {
        return new ChildRecord(null, naturalId, null, null,
                new TreeSet<>(), new TreeSet<>(), 0);
    }

    @Override
    public ChildRecord upsertedWith(final ChildRecord upserted) {
        id = upserted.id;
        naturalId = upserted.naturalId;
        parentNaturalId = upserted.parentNaturalId;
        value = upserted.value;
        sideValues = new TreeSet<>(upserted.sideValues);
        version = upserted.version;
        return this;
    }
}
