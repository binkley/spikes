package x.loggy;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookAutoConfiguration;
import x.loggy.HttpTrace.RequestTrace;
import x.loggy.HttpTrace.ResponseTrace;

import java.io.IOError;
import java.util.Set;

import static ch.qos.logback.core.util.OptionHelper.extractDefaultReplacement;
import static java.lang.String.format;
import static x.loggy.HttpTrace.httpTraceOf;

public class LogbookPropertyConverter
        extends ClassicConverter {
    private static final String EMPTY_VALUE = "";
    private static final Set<String> logbookNames = Set.of(
            LogbookAutoConfiguration.class.getName(),
            Logbook.class.getName());

    // TODO: Do we need to configure anything in the mapper?
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        // Strange but true -- the logger is actually the auto-config one
        if (!logbookNames.contains(event.getLoggerName())) {
            return EMPTY_VALUE;
        }

        try {
            final var trace = httpTraceOf(objectMapper, event.getMessage());
            switch (property) {
            case "correlation":
                return trace.getCorrelation();
            case "origin":
                return trace.getOrigin();
            case "method":
                return trace instanceof RequestTrace
                        ? ((RequestTrace) trace).getMethod()
                        : EMPTY_VALUE;
            case "uriPath":
                return trace instanceof RequestTrace
                        ? ((RequestTrace) trace).getUri().getRawPath()
                        : EMPTY_VALUE;
            case "status":
                return trace instanceof ResponseTrace
                        ? String.valueOf(((ResponseTrace) trace).getStatus())
                        : EMPTY_VALUE;
            case "duration":
                return trace instanceof ResponseTrace
                        ? String
                        .valueOf(((ResponseTrace) trace).getDuration())
                        : EMPTY_VALUE;
            default:
                throw new Bug(format(
                        "Unknown Logbook JSON property: '%s' for logging "
                                + "event: '%s';"
                                + " pass in the 'logging.debug=true' system"
                                + " property to see this"
                                + " complaint (Logback swallows this "
                                + "exception otherwise, and "
                                + "ignores"
                                + " the logging)", property, event));
            }
        } catch (final IOError e) {
            throw new Bug(format(
                    "Logbook generated invalid JSON: '%s' for logging "
                            + "event: '%s';"
                            + " pass in the 'logging.debug=true' system "
                            + "property to see this"
                            + " complaint (Logback swallows this exception "
                            + "otherwise, and ignores"
                            + " the logging)", e, event));
        }
    }
}
