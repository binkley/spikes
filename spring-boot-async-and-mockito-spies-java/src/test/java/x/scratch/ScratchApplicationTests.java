package x.scratch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import x.scratch.ScratchApplication.Bob;
import x.scratch.ScratchApplication.Sally;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ScratchApplicationTests {
    private static final AtomicBoolean executorRan = new AtomicBoolean(false);
    private static final AtomicBoolean taskRan = new AtomicBoolean(false);

    @SpyBean
    private Bob bob;
    @Autowired
    private Sally sally;

    @Test
    public void shouldWaitOnBob() {
        assertThat(mockingDetails(bob).isSpy()).as("Should be a spy")
                .isTrue();

        sally.runIt();

        try {
            verify(bob, timeout(SECONDS.toMillis(2L))).runItEventually();
        } catch (final AssertionError e) {
            if (!executorRan.get())
                throw new AssertionError("Did not even run the executor");
            if (!taskRan.get())
                throw new AssertionError("Did not even run the task");
            throw e;
        }
    }

    @TestConfiguration
    public static class MyTestConfiguration {
        @Bean
        public TaskExecutor slowExecutor() {
            return task -> {
                executorRan.set(true);
                new Thread(() -> {
                    try {
                        SECONDS.sleep(1L);
                        taskRan.set(true);
                        task.run();
                    } catch (final InterruptedException e) {
                        currentThread().interrupt();
                    }
                }).start();
            };
        }
    }
}
