package x.xmlish;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Instant;

@JacksonXmlRootElement
@RequiredArgsConstructor
@Value
class Xmlish {
    String foo;
    int barNone;
    Instant when;
}
