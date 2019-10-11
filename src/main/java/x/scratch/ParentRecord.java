package x.scratch;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("parent")
public class ParentRecord {
    @Id
    private Long id;
    @NonNull
    private String naturalId;
    private String value;
    private Integer version;
}
