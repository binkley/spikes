package x.xmlish;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode
@ToString
public final class OuterJaxb {
    public Upper upper;
    public List<Inner> inner;

    @EqualsAndHashCode
    @ToString
    public static final class Upper {
        public String foo;
        public Integer bar;
    }

    @EqualsAndHashCode
    @ToString
    public static final class Inner {
        public String foo;
        public Integer quux;
        @XmlJavaTypeAdapter(InstantAdapter.class)
        public Instant when;
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        public LocalDate day;
    }
}
