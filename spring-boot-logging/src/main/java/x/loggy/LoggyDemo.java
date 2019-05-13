package x.loggy;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.style.ToStringCreator;
import org.springframework.stereotype.Component;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.CoderMalfunctionError;

import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;

@Component
@ConditionalOnProperty(prefix = "loggy", name = "enable-demo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggyDemo {
    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;
    private final UnknownHostRemote unknownHost;
    private final Logger logger;

    @EventListener
    public void ready(final ApplicationReadyEvent event) {
        logger.warn("SIMPLE LOGGING");
        logger.info("I am ready: {}", new ToStringCreator(event)
                .append("args", event.getArgs())
                .append("source", event.getSource())
                .append("timestamp", event.getTimestamp()));
        logger.debug("And this is json: {\"a\":3}"); // Logged as string
        logger.debug("{\"a\":3}"); // Logged as embedded JSON, not string

        logger.warn("CALL OURSELVES");

        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "abcdef0987654321",
                        "X-B3-SpanId", "abcdef0987654321",
                        "X-B3-ParentSpanId", "abcdef0987654321")
                .build();
        final var client = HttpClient.newBuilder()
                .build();

        final HttpResponse<String> response = sendOrDie(request, client);

        logger.info("{}", response.body());
        logger.debug("(Really got {} after sending {})", response, request);

        logger.warn("EXCEPTIONS");

        // Show stack trace logging
        final var e = new CoderMalfunctionError(
                new NullPointerException("OH MY, A NULL POINTER!"));
        logger.error("And I fail: {}", e.getMessage(), e);

        // Feign
        logger.warn("FEIGN");

        logger.warn("CALL OURSELVES WITH FEIGN DIRECT");

        final var direct = loggy.getDirect();
        logger.info("{}", direct);

        logger.warn("CALL OURSELVES WITH FEIGN THROUGH FEIGN");

        final var indirect = loggy.getIndirect();
        logger.info("{}", indirect);

        final var loggyResponse = loggy.getDirect();
        logger.info("{}", loggyResponse);

        try {
            notFound.get();
        } catch (final FeignException notFound) {
            logger.error("Feign angry: {}: {}",
                    getMostSpecificCause(notFound).toString(),
                    notFound.contentUTF8(),
                    notFound);
        }

        try {
            unknownHost.get();
        } catch (final FeignException ignored) {
            // Already logged by logbook-feign logger
        }

        logger.warn("THIS AND THAT");

        final var otherRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/not-found"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "abcdef0987654321",
                        "X-B3-SpanId", "abcdef0987654321",
                        "X-B3-ParentSpanId", "abcdef0987654321")
                .build();
        final var otherClient = HttpClient.newBuilder()
                .build();

        final HttpResponse<String> otherResponse = sendOrDie(otherRequest,
                otherClient);

        logger.warn("BUT IT'S ALRIGHT, IT'S OK, I'M GONNA RUN THAT WAY");
    }

    private HttpResponse<String> sendOrDie(final HttpRequest request,
            final HttpClient client) {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (final IOException e) {
            logger.error("SERVER NOT READY? {}",
                    getMostSpecificCause(e).toString(), e);
            throw new IOError(e);
        } catch (final InterruptedException e) {
            logger.error("INTERRUPTED? {}",
                    getMostSpecificCause(e).toString(), e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
