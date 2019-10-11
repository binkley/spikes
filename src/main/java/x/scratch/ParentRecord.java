package x.scratch;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Data
@Table("parent")
public class ParentRecord {
    @Id
    private Long id;
    @NonNull
    private String naturalId;
    private String value;
    private Integer version;

    public ParentRecord updateWith(final ParentRecord upserted) {
        id = upserted.id;
        naturalId = upserted.naturalId;
        value = upserted.value;
        version = upserted.version;
        return this;
    }
}
