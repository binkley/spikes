package x.scratch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import x.scratch.ScratchApplication.Bob;
import x.scratch.ScratchApplication.Sally;
import x.scratch.TestAsyncConfiguration.TestingExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static x.scratch.AsyncConfiguration.SLOW_EXECUTOR_BEAN_NAME;

@Import(TestAsyncConfiguration.class)
@SpringBootTest
class ScratchApplicationTests {
    @SpyBean
    private Bob bob;
    @Autowired
    private Sally sally;
    @Autowired
    private TestingExecutor testingExecutor;
    @Autowired
    @Qualifier(SLOW_EXECUTOR_BEAN_NAME)
    private TestingExecutor specialTestingExecutor;

    @Test
    void shouldSpy() {
        assertThat(mockingDetails(bob).isSpy())
                .as("Should be a spy")
                .isTrue();
    }

    @Test
    void shouldWaitOnBobButNotTooMuch()
            throws InterruptedException {
        sally.runItEventuallyButQuickly();

        final var executor = testingExecutor
                .awaitExecutorCalled(1L, SECONDS);
        assertThat(executor.getName()).isEqualTo("default");
        testingExecutor.awaitTaskRan(1L, SECONDS);

        verify(bob).runItEventuallyButQuickly();
    }

    @Test
    void shouldGiveUpWaitingOnBob()
            throws InterruptedException {
        sally.runItEventuallyButSlowly();

        final var executor = specialTestingExecutor
                .awaitExecutorCalled(1L, SECONDS);
        assertThat(executor.getName()).isEqualTo(SLOW_EXECUTOR_BEAN_NAME);
        specialTestingExecutor.awaitTaskRan(1L, SECONDS);

        verify(bob).runItEventuallyButSlowly();
    }
}
