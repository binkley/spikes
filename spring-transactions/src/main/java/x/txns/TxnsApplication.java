package x.txns;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootApplication
public class TxnsApplication
        implements CommandLineRunner {
    private final Logger logger;

    public static void main(final String... args) {
        // FYI -- using the try-block shuts down the program after
        // the command-line runner finishes: Faster feedback cycle
        try (final var ignored = SpringApplication
                .run(TxnsApplication.class, args)) {
        }
    }

    @Override
    public void run(final String... args) {
        logger.info("BUT IT'S ALRIGHT, IT'S OK, I'M GONNA RUN THAT WAY");
    }
}
