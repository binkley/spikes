package x.loggy.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("BOB")
public class BobRecord {
    @Id
    Long id;
    String name;
}
