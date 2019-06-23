package x.xmlish;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

public final class InstantAdapter
        extends XmlAdapter<String, Instant> {
    @Override
    public Instant unmarshal(final String when) {
        return Instant.parse(when);
    }

    @Override
    public String marshal(final Instant when) {
        return when.toString();
    }
}
