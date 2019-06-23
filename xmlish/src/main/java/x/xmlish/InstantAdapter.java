package x.xmlish;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public final class InstantAdapter
        extends XmlAdapter<String, Instant> {
    @Override
    public Instant unmarshal(final String when) {
        return ISO_INSTANT.parse(when, Instant::from);
    }

    @Override
    public String marshal(final Instant when) {
        return ISO_INSTANT.format(when);
    }
}
