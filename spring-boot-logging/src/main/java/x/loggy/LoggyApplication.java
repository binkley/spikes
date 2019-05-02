package x.loggy;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootApplication
public class LoggyApplication
        implements CommandLineRunner {
    private final SampleHttpBin happyPath;
    private final NowheresVille sadPath;
    private final Logger logger;

    public static void main(final String... args) {
        // Copy env to sysprops so it appears as a Spring prop to Logback
        // MDC is unreliable when using thread pools, by sysprops work
        final var environment = System.getenv()
                .getOrDefault("ENVIRONMENT", "local");
        System.setProperty("environment", environment);

        // FYI -- using the try-block shuts down the program after
        // the command-line runner finishes: Faster feedback cycle
        try (final var ignored = SpringApplication
                .run(LoggyApplication.class, args)) {
        }
    }

    @Override
    public void run(final String... args)
            throws IOException, InterruptedException {
        logger.info("I am in COMMAND");
        logger.debug("And this is json: {\"a\":3}"); // Logged as string
        logger.debug("{\"a\":3}"); // Logged as embedded JSON, not string

        // Talk to ourselves
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080"))
                .build();
        final var client = HttpClient.newBuilder()
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        logger.debug("(Really got {} after sending {})", response, request);
        logger.info("{}", response.body());

        // Show stack trace logging
        final var e = new NullPointerException("OH MY, A NULL POINTER!");
        logger.error("And I fail: {}", e.getMessage(), e);

        // Feign
        try {
            final var happyFeign = happyPath.get();
            logger.info("He said, {}", happyFeign);

            sadPath.get();
        } catch (final FeignException badFeign) {
            // Already logged by logbook-feign logger
        }

        logger.info("BUT IT'S ALRIGHT, IT'S OK, I'M GONNA RUN THAT WAY");
    }
}
