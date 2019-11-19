package x.domainpersistencemodeling

import ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.tutteli.atrium.creating.ReportingAssertionPlant
import ch.tutteli.atrium.verbs.expect
import net.sf.jsqlparser.JSQLParserException
import net.sf.jsqlparser.parser.CCJSqlParserUtil.parse
import org.slf4j.LoggerFactory.getLogger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.ArrayList
import java.util.Locale
import java.util.regex.Pattern

@Component
class SqlQueries
    : AutoCloseable {
    private val queries: MutableList<String> = ArrayList()
    private val appender = Appender()

    /** Creates a `List` expectation for Atrium, and resets the listener. */
    val expectNext: ReportingAssertionPlant<List<String>>
        get() = expect(queries.toList()).also {
            reset()
        }

    override fun close() {
        appender.stop()
    }

    fun <V> expectNextByType(toValue: (List<String>) -> V) =
            expect(queries.groupBy {
                bucket(it)
            }.extractUpserts().map {
                it.key to toValue(it.value)
            }.toMap()).also {
                reset()
            }

    fun reset() = queries.clear()

    @Suppress("unused")
    fun dump() = println(queries)

    private inner class Appender : AppenderBase<ILoggingEvent>() {
        private val logger =
                getLogger(JdbcTemplate::class.java.name) as Logger
        private val oldLevel = logger.level
        private val oldAdditive = logger.isAdditive

        override fun append(eventObject: ILoggingEvent) {
            val message = eventObject.message.lines().filter {
                it.isNotBlank()
            }.joinToString(" ") {
                it.trim()
            }
            val matcher = queryOnly.matcher(message)
            if (!matcher.find()) return
            val query = matcher.group(1).trim()
            queries.add(query)
        }

        override fun stop() {
            super.stop()
            logger.level = oldLevel
            logger.isAdditive = oldAdditive
        }

        init {
            logger.level = DEBUG
            logger.isAdditive = false // Avoid extra DEBUG console logging
            logger.addAppender(this)
            start()
        }
    }

    private companion object {
        val queryOnly = Pattern.compile(
                "^Executing prepared SQL statement \\[(.*)]$")!!
        private val upsert = Pattern.compile("^SELECT \\* FROM upsert_.*$")

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

        fun bucket(query: String) = try {
            parse(query).javaClass.simpleName
                    .replace("Statement", "")
                    .toUpperCase(Locale.US) // TODO: What is ASCII upcase?
        } catch (e: JSQLParserException) {
            "INVALID"
        }
    }
}
