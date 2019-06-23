package x.xmlish;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

//@JsonIgnoreProperties(ignoreUnknown = true)
@Value
class OuterJackson {
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
        // TODO: Constant to reuse?  Surprisingly, no?
        @JsonFormat(shape = STRING, pattern = "yyyyMMdd")
        LocalDate day;
    }
}
