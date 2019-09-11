package x.loggy.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.LinkedHashSet;
import java.util.Set;

@EqualsAndHashCode
@Table("HOWARD")
@ToString
public class HowardRecord {
    @Id
    public Long id;
    public String name;
    @Column("howard_id")
    public Set<NancyRecord> nancies = new LinkedHashSet<>();

    public HowardRef ref() {
        final var ref = new HowardRef();
        ref.howardId = id;
        return ref;
    }

    @Data
    @Table("SALLY_HOWARD")
    public static class HowardRef {
        public Long howardId;
    }
}
