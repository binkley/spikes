package x.loggy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SeverityConverter
        extends ClassicConverter {
    @Override
    public String convert(final ILoggingEvent event) {
        final var level = event.getLevel();
        if (Level.ERROR.equals(level)) return "HIGH";
        if (Level.WARN.equals(level)) return "MEDIUM";
        if (Level.INFO.equals(level)) return "LOW";
        else throw new IllegalStateException(
                "Logback appender missing a level filter: unexpected log level: "
                        + level);
    }
}
