package x.loggy;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookAutoConfiguration;

import java.util.Set;

import static ch.qos.logback.core.util.OptionHelper.extractDefaultReplacement;
import static java.lang.String.format;

public class TryLogbookMessageConverter
        extends ClassicConverter {
    static final String EMPTY_VALUE = "";

    private static final Set<String> logbookNames = Set.of(
            LogbookAutoConfiguration.class.getName(),
            Logbook.class.getName());

    private String property;

    @Override
    public void start() {
        final var keyInfo = extractDefaultReplacement(getFirstOption());
        property = keyInfo[0];
        super.start();
    }

    @Override
    public void stop() {
        property = null;
        super.stop();
    }

    @Override
    public String convert(final ILoggingEvent event) {
        // The simplest thing that could possibly work is to only claim the
        // message is JSON when the logger is Logbook request/response.  A
        // more sophisticated approach would be to try parsing the message
        // with ObjectMapper, and only say it's JSON when parsing works.
        final var logbookLogging = logbookNames.contains(
                event.getLoggerName());

        switch (property) {
        case "text":
            return logbookLogging ? EMPTY_VALUE : event.getFormattedMessage();
        case "json":
            return logbookLogging ? event.getMessage() : EMPTY_VALUE;
        default:
            throw new Bug(format(
                    "Unknown converter property: '%s' for logging event: '%s'"
                            + "; pass in the 'logging.debug=true' system"
                            + " property to see this complaint (Logback"
                            + " swallows this exception otherwise, and"
                            + " ignores the logging)", property, event));
        }
    }
}
