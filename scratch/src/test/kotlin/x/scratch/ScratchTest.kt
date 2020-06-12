package x.scratch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
internal class ScratchTest {
    @Test
    fun `should test suspending functions`() = runBlockingTest {
        assertThrows<IllegalStateException> { failing() }
    }
}

private suspend fun failing() {
    delay(1L) // pretend
    error("Fail on purpose")
}
