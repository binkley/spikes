package x.validating;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Value
class Validish {
    String foo;
    int barNone;
    Instant when;
    @NotEmpty
    @Valid
    List<Inner> inners = new ArrayList<>();

    @Value
    static class Inner {
        @NotEmpty
        String qux;
        int quux;
        Instant ever;
    }
}
