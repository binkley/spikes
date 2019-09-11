package x.loggy.data;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode
@Table("NANCY")
@ToString
public class NancyRecord {
    public String name;
    public Long howardId;
}
