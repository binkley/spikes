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
    private Integer version;

    @Override
    public ParentRecord updateWith(final ParentRecord upserted) {
        id = upserted.id;
        naturalId = upserted.naturalId;
        value = upserted.value;
        version = upserted.version;
        return this;
    }
}
