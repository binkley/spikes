package x.retryable

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.micronaut.retry.intercept.DefaultRetryInterceptor
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory.getLogger
import javax.inject.Inject

@MicronautTest
internal class RetryingTest {
    @Inject
    lateinit var retrying: RetryingClient

    @Test
    fun `should retry 3 times`() {
        val retryingLogger = getLogger(DefaultRetryInterceptor::class.java)
                as ch.qos.logback.classic.Logger
        val listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        retryingLogger.addAppender(listAppender)

        retrying.retryMe()

        listAppender.list.forEach {
            println(it)
        }
    }
}
