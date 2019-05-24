package x.xmlish;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Value;

import java.util.List;

@Value
public class ComplexExample {
    Head head;
    Body body;

    @Value
    public static class Head {
        String title;
    }

    @Value
    public static class Body {
        BookReview bookreview;

        @Value
        public static class BookReview {
            String title;
            Table table;

            @Value
            public static class Table {
                List<Tr> tr;

                @JsonIgnoreProperties(ignoreUnknown = true)
                @Value
                public static class Tr {
                    @JacksonXmlProperty(isAttribute = true)
                    String align;
                }
            }
        }
    }
}
