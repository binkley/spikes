package x.txns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import static java.util.concurrent.TimeUnit.SECONDS;

@EnableAsync
@SpringBootApplication
public class TxnsApplication {
    public static void main(final String... args)
            throws InterruptedException {
        // FYI -- using the try-block shuts down the program after
        // the command-line runner finishes: Faster feedback cycle
        try (final var context = SpringApplication
                .run(TxnsApplication.class, args)) {
            // Let post-commit event listeners run in thread pool
            SECONDS.sleep(2L);
        }
    }
}
