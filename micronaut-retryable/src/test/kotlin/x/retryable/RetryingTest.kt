package x.retryable

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test

internal class RetryingTest {
    @Test
    fun `should retry 3 times`() {
        val retrying = Retrying()

        expect {
            retrying.retryMe()
        }.toThrow<IllegalStateException> { }

        expect(retrying.retried).toBe(3)
    }
}
