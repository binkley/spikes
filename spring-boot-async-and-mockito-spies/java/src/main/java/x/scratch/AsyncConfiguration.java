package x.scratch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;

@Configuration
@EnableAsync
public class AsyncConfiguration {
    public static final String SLOW_EXECUTOR_BEAN_NAME = "slowExecutor";

    @Bean
    @Primary
    public Executor exector() {
        return Runnable::run;
    }

    @Bean
    @ConditionalOnMissingBean(name = SLOW_EXECUTOR_BEAN_NAME)
    public Executor slowExecutor() {
        return command -> {
            try {
                TimeUnit.MINUTES.sleep(1L);
                command.run();
            } catch (final InterruptedException e) {
                currentThread().interrupt();
            }
        };
    }
}
