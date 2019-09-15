package hello.world;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Introspected
public class RoundTripData {
    private @NotEmpty String a;
    private @Positive int b;
}
