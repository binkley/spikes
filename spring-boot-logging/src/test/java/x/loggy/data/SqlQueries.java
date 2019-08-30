package x.loggy.data;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static ch.qos.logback.classic.Level.DEBUG;
import static java.util.regex.Pattern.compile;
import static org.slf4j.LoggerFactory.getLogger;

public final class SqlQueries
        extends AbstractList<String> {
    private static final Pattern queryOnly = compile(
            "^Executing prepared SQL statement \\[(.*)]$");
    private final List<String> queries = new ArrayList<>();
    @SuppressWarnings("unused")
    private final Appender appender = new Appender();

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
            queries.add(matcher.group(1));
        }
    }
}
