package x.retryable

import ch.tutteli.atrium.api.cc.en_GB.hasSize
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import io.micronaut.context.env.Environment
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
internal class RetryingTest {
    @Inject
    lateinit var retrying: RetryingClient
    @Inject
    lateinit var testRetryEventListener: TestRetryEventListener
    @Inject
    lateinit var env: Environment

    @AfterEach
    fun tearDown() {
        testRetryEventListener.reset()
    }

    @Test
    fun `should retry 3 times in logging`() { // No, not really :)
        val testRetryAppender = TestRetryAppender()

        expect {
            retrying.retryMe()
        }.toThrow<HttpClientException> { }

        expect(testRetryAppender.events.filter {
            it.message.startsWith("Retrying")
        }).hasSize(attempts)
    }

    @Test
    fun `should retry 3 times in events`() { // No, not really :)
        expect {
            retrying.retryMe()
        }.toThrow<HttpClientException> { }

        expect(testRetryEventListener.events)
            .hasSize(attempts)
    }

    // TODO: Would be nicer with @Property or @Value
    private val attempts: Int
        get() = env.getProperty("retrying.attempts", String::class.java)
            .orElseThrow().toInt()
}
