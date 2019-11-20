package x.scratch;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import net.sf.jsqlparser.JSQLParserException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static ch.qos.logback.classic.Level.DEBUG;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static net.sf.jsqlparser.parser.CCJSqlParserUtil.parse;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SqlQueries
        implements AutoCloseable {
    private static final Pattern queryOnly = compile(
            "^Executing prepared SQL statement \\[(.*)]$");
    private static final Pattern upsert = compile(
            "^SELECT \\* FROM upsert_.*$");

    private final List<String> queries = new ArrayList<>();
    private final Appender appender = new Appender();

    private static String bucket(final String query) {
        try {
            final var matcher = upsert.matcher(query);
            if (matcher.find()) return "UPSERT";
            return parse(query).getClass()
                    .getSimpleName()
                    .replace("Statement", "")
                    .toUpperCase(Locale.US);
        } catch (final JSQLParserException e) {
            return "INVALID";
        }
    }

    /** Stops query collection, discarding accumulated queries. */
    @Override
    public void close() {
        appender.stop();
        reset();
    }

    /**
     * Returns all SQL queries, consuming accumulated queries, and resetting
     * their collection.
     */
    public List<String> getQueries() {
        final var queries = new ArrayList<>(this.queries);
        reset();
        return queries;
    }

    /**
     * Collates the queries by SQL type, consuming accumulated queries, and
     * resetting their collection.
     */
    public Map<String, List<String>> queriesByType() {
        final var queriesByType = getQueries().stream().collect(groupingBy(
                SqlQueries::bucket, LinkedHashMap::new, toList()));
        reset();
        return queriesByType;
    }

    /** Discards accumulated queries, and resets their collection. */
    public void reset() {
        queries.clear();
    }

    private final class Appender
            extends AppenderBase<ILoggingEvent> {
        private final Logger logger = (Logger) getLogger(JdbcTemplate.class);
        private final Level oldLevel = logger.getLevel();
        private final boolean oldAdditive = logger.isAdditive();

        @SuppressWarnings("ThisEscapedInObjectConstruction")
        Appender() {
            logger.setLevel(DEBUG);
            logger.setAdditive(false); // Avoid extra DEBUG console logging
            logger.addAppender(this);
            start();
        }

        @Override
        protected void append(final ILoggingEvent eventObject) {
            final var message = eventObject.getMessage().lines()
                    .filter(it -> !it.isBlank())
                    .collect(joining(" "))
                    .trim();
            final var matcher = queryOnly.matcher(message);
            if (!matcher.find()) return;
            final var query = matcher.group(1).trim();
            queries.add(query);
        }

        @Override
        public void stop() {
            super.stop();
            logger.setLevel(oldLevel);
            logger.setAdditive(oldAdditive);
        }
    }
}

    /*

        /** Creates a `List` expectation for Atrium, and resets the
        listener.
    val expectNext:ReportingAssertionPlant<List<String>>
        get()=expect(queries.toList()).also{
        reset()
        }

        fun<V> expectNextByType(toValue:(List<String>)->V)=
        expect(queries.groupBy{
        bucket(it)
        }.extractUpserts().map{
        it.key to toValue(it.value)
        }.toMap()).also{
        reset()
        }

        fun Map<String, List<String>>.extractUpserts() = map {
            if ("SELECT" == it.key) {
                it.value.extractUpserts()
            } else {
                listOf(it.key to it.value)
            }
        }.flatten().toMap()

        fun List<String>.extractUpserts() = map {
            val matcher = upsert.matcher(it)
            if (matcher.find()) "UPSERT" to it
            else "SELECT" to it
        }.groupBy {
            it.first
        }.map {
            it.key to it.value.map { itt ->
                itt.second
            }
        }
    }
*/
