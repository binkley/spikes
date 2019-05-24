package x.xmlish;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Builder
@JacksonXmlRootElement(localName = "xmlish")
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
