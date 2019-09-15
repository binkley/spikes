package hello.world.data;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.Data;

@Data
@MappedEntity
public class FooRecord {
    @Id
    private String code;
    private String foo;
}
