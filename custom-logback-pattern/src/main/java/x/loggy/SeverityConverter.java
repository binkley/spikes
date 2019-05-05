package x.loggy;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.Marker;

import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;
import static java.lang.String.format;
import static org.slf4j.MarkerFactory.getMarker;

public class SeverityConverter
        extends ClassicConverter {
    private static final String UrgentSeverity = "Urgent";
    public static final Marker URGENT = getMarker(UrgentSeverity);
    private static final String HighSeverity = "High";
    public static final Marker HIGH = getMarker(HighSeverity);
    private static final String MediumSeverity = "Medium";
    public static final Marker MEDIUM = getMarker(MediumSeverity);
    private static final String LowSeverity = "Low";
    public static final Marker LOW = getMarker(LowSeverity);

    @Override
    public String convert(final ILoggingEvent event) {
        final var marker = event.getMarker();
        if (null != marker) {
            return marker.getName();
        }

        final var level = event.getLevel();
        if (ERROR.equals(level)) return HighSeverity;
        if (WARN.equals(level)) return MediumSeverity;
        if (INFO.equals(level)) return LowSeverity;
        else throw new Bug(format(
                "logback.xml appender for STDOUT missing a threshold filter"
                        + " which ignores DEBUG or lower loggings: unexpected"
                        + " log level for logging event: '%s'; pass in the "
                        + "'logging.debug=true' system property to see this"
                        + " complaint (Logback swallows this exception"
                        + " otherwise, and ignores the logging)", event));
    }
}
