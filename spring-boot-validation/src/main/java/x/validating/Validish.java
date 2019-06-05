package x.validating;

import lombok.Value;

import java.time.Instant;

@Value
class Validish {
    String foo;
    int barNone;
    Instant when;
    Inner inner;

    @Value
    static class Inner {
        String qux;
        int quux;
        Instant ever;
    }
}
