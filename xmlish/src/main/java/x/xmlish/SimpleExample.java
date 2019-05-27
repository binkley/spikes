package x.xmlish;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Value;

import javax.validation.Valid;
import java.util.List;

@JacksonXmlRootElement(localName = "foo")
@Value
public class SimpleExample {
    List<@Valid Bar> bar;

    @Value
    public static class Bar {
        String text;

        @JacksonXmlText
        @JsonCreator
        public static Bar valueOf(final String text) {
            return new Bar(text);
        }
    }
}
