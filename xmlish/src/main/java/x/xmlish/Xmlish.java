package x.xmlish;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Builder
@Value
class Xmlish {
    String foo;
    int barNone;
    Instant when;
    Inner inner;

    @Builder
    @Value
    static class Inner {
        String qux;
        int quux;
        Instant ever;
    }
}
