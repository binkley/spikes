package x.scratch.parent;

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
@Table("parent")
public class ParentRecord
        implements ParentSimpleDetails,
        UpsertableRecord<ParentRecord> {
    @Id
    private Long id;
    private @NonNull String naturalId;
    private String value;
    @Builder.Default
    private @NonNull Set<String> sideValues = new TreeSet<>();
    private int version;

    static ParentRecord createRecordFor(final String naturalId) {
        return new ParentRecord(null, naturalId, null, new TreeSet<>(), 0);
    }

    @Override
    public ParentRecord upsertedWith(final ParentRecord upserted) {
        id = upserted.id;
        naturalId = upserted.naturalId;
        value = upserted.value;
        version = upserted.version;
        return this;
    }
}
