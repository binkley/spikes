package x.retryable

import ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.micronaut.retry.intercept.DefaultRetryInterceptor
import org.slf4j.LoggerFactory.getLogger

internal class TestRetryAppender : AppenderBase<ILoggingEvent>() {
    private val logger = getLogger(DefaultRetryInterceptor::class.java)
            as Logger
    private val oldLevel = logger.level
    private val oldAdditive = logger.isAdditive

    private val _events = mutableListOf<ILoggingEvent>()
    val events: List<ILoggingEvent>
        get() = _events

    fun reset() = _events.clear()

    override fun append(eventObject: ILoggingEvent) {
        _events += eventObject
    }

    override fun stop() {
        super.stop()
        reset()
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
