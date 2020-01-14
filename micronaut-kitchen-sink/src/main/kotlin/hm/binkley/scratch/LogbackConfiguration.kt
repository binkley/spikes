package hm.binkley.scratch

import ch.qos.logback.classic.BasicConfigurator
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.ConsoleAppender
import net.logstash.logback.encoder.LogstashEncoder

class LogbackConfiguration(
    private val json: Boolean = true
) : BasicConfigurator() {
    override fun configure(lc: LoggerContext) {
        val console = ConsoleAppender<ILoggingEvent>().apply {
            context = lc
            name = "STDOUT"
            if (json) {
                encoder = LogstashEncoder().apply {
                    context = lc
                }.also { it.start() }
            } else {
                isWithJansi = true
                encoder = PatternLayoutEncoder().apply {
                    context = lc
                    pattern = """
%cyan(%d{HH:mm:ss.SSS}) %gray([%thread]) %highlight(%-5level) %magenta(%logger{36}) - %msg%n
                    """.trim()
                }.also { it.start() }
            }
        }.also { it.start() }

        lc.getLogger(ROOT_LOGGER_NAME).apply {
            level = INFO
            this += console
        }
    }
}

private operator fun Logger.plusAssign(appender: Appender<ILoggingEvent>) =
    addAppender(appender)
