package x.validating;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Value
class Validish {
    String foo;
    int barNone;
    Instant when;
    @NotNull
    @Valid
    Inner inner;

    @Value
    static class Inner {
        @NotEmpty
        String qux;
        int quux;
        Instant ever;
    }
}
