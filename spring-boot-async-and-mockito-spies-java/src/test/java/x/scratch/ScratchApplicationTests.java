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

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ScratchApplicationTests {
    private static boolean executorRan = false;
    private static boolean taskRan = false;

    @SpyBean
    private Bob bob;
    @Autowired
    private Sally sally;

    @Test
    public void shouldWaitOnBob() {
        sally.runIt();

        try {
            verify(bob, timeout(2_000L)).runItEventually();
        } catch (final AssertionError e) {
            if (!executorRan)
                throw new AssertionError("Did not even run the executor");
            if (!taskRan)
                throw new AssertionError("Did not even run the task");
            throw e;
        }
    }

    @TestConfiguration
    public static class MyTestConfiguration {
        @Bean
        public TaskExecutor slowExecutor() {
            return task -> {
                out.println("MyTestConfiguration.slowExecutor -- executor");
                executorRan = true;
                new Thread(() -> {
                    try {
                        out.println(
                                "MyTestConfiguration.slowExecutor -- task");
                        Thread.sleep(1_000L);
                        task.run();
                        taskRan = true;
                    } catch (final InterruptedException e) {
                        currentThread().interrupt();
                    }
                });
            };
        }
    }
}
