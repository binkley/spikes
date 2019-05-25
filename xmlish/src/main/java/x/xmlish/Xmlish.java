package x.xmlish;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Value;

import java.time.Instant;

@JacksonXmlRootElement(localName = "xmlish")
@Value
class Xmlish {
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
