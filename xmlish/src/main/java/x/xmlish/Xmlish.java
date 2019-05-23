package x.xmlish;

import lombok.Value;

import java.time.Instant;

@Value
class Xmlish {
    String foo;
    int barNone;
    Instant when;
}
