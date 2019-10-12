package x.scratch;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Data
@Table("parent")
public class ParentRecord implements UpsertableRecord<ParentRecord> {
    @Id
    private Long id;
    private @NonNull String naturalId;
    private String value;
    private int version;

    static ParentRecord createRecordFor(final String naturalId) {
        return new ParentRecord(null, naturalId, null, 0);
    }

    @Override
    public ParentRecord updateWith(final ParentRecord upserted) {
        id = upserted.id;
        naturalId = upserted.naturalId;
        value = upserted.value;
        version = upserted.version;
        return this;
    }
}
