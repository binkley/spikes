package x.retryable

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
internal class RetryingTest {
    @Inject
    lateinit var retryingClient: RetryingClient

    @Test
    fun `should retry 3 times`() {
        val x = retryingClient.retryMe()
        println("HERE!")
        println(x)
    }
}
