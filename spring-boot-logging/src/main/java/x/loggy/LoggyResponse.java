package x.loggy;

import lombok.Value;

import java.time.Instant;

@Value
public class LoggyResponse {
    String foo;
    int barNone;
    Instant when;
}
