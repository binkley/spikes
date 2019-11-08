package x.domainpersistencemodeling

import ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import net.sf.jsqlparser.JSQLParserException
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import org.slf4j.LoggerFactory.getLogger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.AbstractList
import java.util.ArrayList
import java.util.Locale
import java.util.regex.Pattern

@Component
class SqlQueries
    : AbstractList<String>(),
        AutoCloseable {
    private val queries: MutableList<String> = ArrayList()
    private val appender = Appender()

    override val size: Int
        get() = queries.size

    override fun get(index: Int): String {
        return queries[index]
    }

    override fun clear() {
        queries.clear()
    }

    override fun close() {
        appender.stop()
    }

    val byType: Map<String, List<String>>
        get() = groupBy { bucket(it) }

    private inner class Appender : AppenderBase<ILoggingEvent>() {
        override fun append(eventObject: ILoggingEvent) {
            val message = eventObject.message
            val matcher =
                    queryOnly.matcher(message)
            if (!matcher.find()) return
            val query = matcher.group(1)
            queries.add(query)
        }

        init {
            val logger = getLogger(
                    JdbcTemplate::class.java.name) as Logger
            logger.level = DEBUG
            // TODO: How to restore DEBUG console logging when done?
            logger.isAdditive = false // Avoid extra DEBUG console logging
            logger.addAppender(this)
            start()
        }
    }

    companion object {
        private val queryOnly = Pattern.compile(
                "^Executing prepared SQL statement \\[(.*)]$")
    }
}

fun bucket(query: String): String {
    return try {
        CCJSqlParserUtil.parse(query).javaClass.simpleName
                .replace("Statement", "")
                .toUpperCase(Locale.US)
    } catch (e: JSQLParserException) {
        "INVALID"
    }
}
