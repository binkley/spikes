package x.xmlish;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

public final class InstantAdapter
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
