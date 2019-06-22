package x.xmlish;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

@EqualsAndHashCode
@ToString
public class OuterJaxb {
    public Upper upper;
    public List<Inner> inner;

    @EqualsAndHashCode
    @ToString
    public static class Upper {
        public String foo;
        public Integer bar;
    }

    @EqualsAndHashCode
    @ToString
    public static class Inner {
        public String foo;
        public Integer quux;
        @XmlJavaTypeAdapter(InstantAdapter.class)
        public Instant when;
    }

    public static class InstantAdapter
            extends XmlAdapter<String, Instant> {
        @Override
        public Instant unmarshal(final String instant) {
            return Instant.parse(instant);
        }

        @Override
        public String marshal(final Instant instant) {
            return instant.toString();
        }
    }
}
