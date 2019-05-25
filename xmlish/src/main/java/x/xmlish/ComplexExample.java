package x.xmlish;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
public class ComplexExample {
    @NotNull @Valid
    Head head;
    @NotNull @Valid
    Body body;

    @Value
    public static class Head {
        @NotNull
        String title;
    }

    @Value
    public static class Body {
        @NotNull @Valid
        BookReview bookreview;

        @Value
        public static class BookReview {
            @NotNull
            String title;
            @NotNull @Valid
            Table table;

            @Value
            public static class Table {
                List<@Valid Tr> tr;

                @Value
                public static class Tr {
                    @JacksonXmlProperty(isAttribute = true)
                    String align;
                    @NotEmpty
                    List<@NotNull Object> td;
                }
            }
        }
    }
}
