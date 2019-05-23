package x.xmlish;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Value;

import java.time.Instant;

@JacksonXmlRootElement
@Value
class XmlishResponse {
    String foo;
    int barNone;
    Instant when;
}
