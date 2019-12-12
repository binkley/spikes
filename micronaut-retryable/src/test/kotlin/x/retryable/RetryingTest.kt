package x.retryable

import ch.tutteli.atrium.api.cc.en_GB.hasSize
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
internal class RetryingTest {
    @Inject
    lateinit var retrying: RetryingClient

    @Test
    fun `should retry 3 times`() { // No, not really :)
        val testRetryAppender = TestRetryAppender()

        expect {
            retrying.retryMe()
        }.toThrow<HttpClientException> { }

        expect(testRetryAppender.events.filter {
            it.message.startsWith("Retrying")
        }).hasSize(RetryingClient.attempts.toInt())
    }
}
