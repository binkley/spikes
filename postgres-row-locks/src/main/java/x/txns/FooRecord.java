package x.txns;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@Data
@Table("FOO")
public class FooRecord {
    @Id
    public Long id;
    public String key;
    public int value;
}
