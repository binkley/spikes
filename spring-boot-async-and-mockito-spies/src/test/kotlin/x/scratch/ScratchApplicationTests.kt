package x.scratch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Import
import java.util.concurrent.TimeUnit.SECONDS

@Import(MyTestConfiguration::class)
@SpringBootTest
class ScratchApplicationTests {
    @SpyBean
    private lateinit var bob: Bob
    @Autowired
    private lateinit var sally: Sally
    @Autowired
    private lateinit var testingExecutor: TestingExecutor

    @Test
    fun shouldWaitOnBob() {
        assertThat(mockingDetails(bob).isSpy)
                .`as`("Should be a spy")
                .isTrue()

        sally.runIt()

        testingExecutor.awaitTaskRan(1L, SECONDS)

        verify(bob, timeout(SECONDS.toMillis(2L))).runItEventually()
    }
}
