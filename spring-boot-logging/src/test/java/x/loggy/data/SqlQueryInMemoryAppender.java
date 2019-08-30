package x.loggy.data;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static ch.qos.logback.classic.Level.DEBUG;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.compile;

public final class SqlQueryInMemoryAppender
        extends AppenderBase<ILoggingEvent> {
    private static final Pattern queryOnly = compile(
            "^Executing prepared SQL statement \\[(.*)]$");
    private final List<String> queries = new ArrayList<>();

    public static SqlQueryInMemoryAppender sqlQueryInMemoryAppender() {
        final var appender = new SqlQueryInMemoryAppender();
        appender.start();
        final var logger = (Logger) LoggerFactory
                .getLogger(JdbcTemplate.class.getName());
        logger.setLevel(DEBUG);
        logger.setAdditive(false); // Avoid extra DEBUG console logging
        // TODO: Remember existing appenders, and reattach/restart them
        if(false) logger.detachAndStopAllAppenders();
        logger.addAppender(appender);
        return appender;
    }

    private SqlQueryInMemoryAppender() {
    }

    public List<String> queries() {
        return unmodifiableList(queries);
    }

    public void reset() {
        queries.clear();
    }

    @Override
    protected void append(final ILoggingEvent eventObject) {
        final var message = eventObject.getMessage();
        final var matcher = queryOnly.matcher(message);
        if (!matcher.find()) return;
        queries.add(matcher.group(1));
    }
}
