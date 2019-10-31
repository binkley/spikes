package x.scratch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Import
import java.util.concurrent.TimeUnit.SECONDS

@Import(TestAsyncConfiguration::class)
@SpringBootTest
class ScratchApplicationTests {
    @SpyBean
    private lateinit var bob: Bob
    @Autowired
    private lateinit var sally: Sally
    @Autowired
    private lateinit var testingExecutor: TestingExecutor
    @Autowired
    @Qualifier(SLOW_EXECUTOR_BEAN_NAME)
    private lateinit var specialTestingExecutor: TestingExecutor

    @Test
    fun shouldSpy() {
        assertThat(mockingDetails(bob).isSpy)
                .`as`("Should be a spy")
                .isTrue()
    }

    @Test
    fun shouldWaitOnBobButNotTooMuch() {
        sally.runItEventuallyButQuickly()

        assertThat(testingExecutor.awaitExecutorCalled(1L, SECONDS)
                .toString()).startsWith("default");
        testingExecutor.awaitTaskRan(1L, SECONDS)

        verify(bob).runItEventuallyButQuickly()
    }

    @Test
    fun shouldGiveUpOnWaitingOnBob() {
        sally.runItEventuallyButSlowly()

        assertThat(specialTestingExecutor.awaitExecutorCalled(1L, SECONDS)
                .toString()).startsWith("special")
        specialTestingExecutor.awaitTaskRan(1L, SECONDS)

        verify(bob).runItEventuallyButSlowly()
    }
}
