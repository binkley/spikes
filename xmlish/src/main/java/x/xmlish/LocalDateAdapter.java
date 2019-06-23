package x.xmlish;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.temporal.TemporalQueries.localDate;

public final class LocalDateAdapter
        extends XmlAdapter<String, LocalDate> {
    private static final DateTimeFormatter formatter = BASIC_ISO_DATE;

    @Override
    public LocalDate unmarshal(final String when) {
        return formatter.parse(when, localDate());
    }

    @Override
    public String marshal(final LocalDate when) {
        return formatter.format(when);
    }
}
