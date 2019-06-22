package x.xmlish;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.time.Instant;
import java.util.List;

//@JsonIgnoreProperties(ignoreUnknown = true)
@Value
class Outer {
    Upper upper;
    List<Inner> inner;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Value
    static class Upper {
        String foo;
        Integer bar;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Value
    static class Inner {
        String foo;
        Integer quux;
        Instant when;
    }
}
