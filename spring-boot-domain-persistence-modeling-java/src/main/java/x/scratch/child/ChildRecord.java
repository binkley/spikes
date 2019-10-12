package x.scratch.child;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import x.scratch.UpsertableRecord;

@Builder(toBuilder = true)
@Data
@Table("child")
public class ChildRecord implements UpsertableRecord<ChildRecord> {
    @Id
    private Long id;
    private @NonNull String naturalId;
    private String parentNaturalId;
    private String value;
    @Builder.Default
    @SuppressWarnings("UnusedAssignment")
    private String subchildren = "[]";
    private int version;

    @Override
    public ChildRecord updateWith(final ChildRecord upserted) {
        id = upserted.id;
        naturalId = upserted.naturalId;
        parentNaturalId = upserted.parentNaturalId;
        value = upserted.value;
        subchildren = upserted.subchildren;
        version = upserted.version;
        return this;
    }
}
