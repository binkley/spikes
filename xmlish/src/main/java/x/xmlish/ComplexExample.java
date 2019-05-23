package x.xmlish;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

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

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Value
        public static class BookReview {
            String title;
        }
    }
}
