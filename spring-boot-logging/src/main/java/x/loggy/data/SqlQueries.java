package x.loggy.data;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static ch.qos.logback.classic.Level.DEBUG;
import static java.util.Locale.US;
import static java.util.regex.Pattern.compile;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SqlQueries
        extends AbstractList<String> {
    private static final Pattern queryOnly = compile(
            "^Executing prepared SQL statement \\[(.*)]$");
    private final List<String> queries = new ArrayList<>();
    @SuppressWarnings("unused")
    private final Appender appender = new Appender();

    private final Map<String, DistributionSummary> histograms
            = new HashMap<>();

    public SqlQueries(final MeterRegistry registry) {
        for (final var type : List
                .of("SELECT", "INSERT", "UPDATE", "OTHER", "INVALID"))
            histograms.put(type, DistributionSummary.builder("database.calls")
                    .tags("sql", type.toLowerCase())
                    .publishPercentiles(0.50, 0.90, 0.95, 0.99)
                    .register(registry));
    }

    private static String bucket(final String query) {
        try {
            final var statement = CCJSqlParserUtil.parse(query);
            return statement.getClass().getSimpleName()
                    .replace("Statement", "")
                    .toUpperCase(US);
        } catch (final JSQLParserException e) {
            return "INVALID";
        }
    }

    @Override
    public int size() {
        return queries.size();
    }

    @Override
    public String get(final int index) {
        return queries.get(index);
    }

    @Override
    public void clear() {
        queries.clear();
    }

    private class Appender
            extends AppenderBase<ILoggingEvent> {
        private Appender() {
            final var logger = (Logger) getLogger(
                    JdbcTemplate.class.getName());
            logger.setLevel(DEBUG);
            // TODO: How to restore DEBUG console logging when done?
            logger.setAdditive(false); // Avoid extra DEBUG console logging
            logger.addAppender(this);
            start();
        }

        @Override
        protected void append(final ILoggingEvent eventObject) {
            final var message = eventObject.getMessage();
            final var matcher = queryOnly.matcher(message);
            if (!matcher.find()) return;
            final var query = matcher.group(1);
            queries.add(query);

            final var bucket = bucket(query);
            switch (bucket) {
            case "SELECT":
            case "INSERT":
            case "UPDATE":
            case "INVALID":
                histograms.get(bucket).record(1);
                break;
            default:
                histograms.get("OTHER").record(1);
                break;
            }
        }
    }
}
