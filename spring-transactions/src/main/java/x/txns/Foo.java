package x.txns;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@Data
@Table("FOO")
public class Foo {
    @Id
    public String key;
    public int value;
}
