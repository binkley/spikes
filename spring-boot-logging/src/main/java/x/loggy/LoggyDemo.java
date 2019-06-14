package x.loggy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.style.ToStringCreator;
import org.springframework.stereotype.Component;
import x.loggy.LoggyRequest.Rolly;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.CoderMalfunctionError;
import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;

@Component
@ConditionalOnProperty(prefix = "loggy", name = "enable-demo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggyDemo {
    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;
    private final UnknownHostRemote unknownHost;
    private final ObjectMapper objectMapper;
    private final Logger logger;

    @EventListener
    public void ready(final ApplicationReadyEvent event)
            throws JsonProcessingException {
        logger.warn("SIMPLE LOGGING");
        logger.info("I am ready: {}", new ToStringCreator(event)
                .append("args", event.getArgs())
                .append("source", event.getSource())
                .append("timestamp", event.getTimestamp()));
        logger.debug("And this is json: {\"a\":3}"); // Logged as string
        logger.debug("{\"a\":3}"); // Logged as embedded JSON, not string

        logger.warn("CALL OURSELVES");

        logger.warn("GET WITH WEB");

        final var client = HttpClient.newBuilder().build();

        logger.warn("INVALID INBOUND TRACE ID");

        final var invalidTracingRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "not-a-trace-id",
                        "X-B3-SpanId", "not-a-trace-id")
                .build();

        sendOrDie(invalidTracingRequest, client);

        logger.warn("GET WITH WEB");

        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "abcdef0987654321",
                        "X-B3-SpanId", "abcdef0987654321")
                .build();

        final HttpResponse<String> response = sendOrDie(request, client);

        logger.info("{}", response.body());
        logger.debug("(Really got {} after sending {})", response, request);

        logger.warn("POST WITH WEB");

        final var postRequest = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(
                        new LoggyRequest(2, List.of(
                                new Rolly(LocalDate.of(9876, 5, 4)))))))
                .uri(URI.create("http://localhost:8080/postish"))
                .header("Content-Type", "application/json")
                .build();
        sendOrDie(postRequest, client);

        logger.warn("CONSTRAINT VIOLATION WITH WEB");

        final var poorRequest = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(
                        new LoggyRequest(-1, List.of(
                                new Rolly(LocalDate.of(9876, 5, 4)))))))
                .uri(URI.create("http://localhost:8080/postish"))
                .header("Content-Type", "application/json")
                .build();
        sendOrDie(poorRequest, client);

        logger.warn("MISMATCHED INPUT WITH WEB");

        final var badRequest = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(
                        "{\"blinken-lights\":2,\"when\":\"not-a-date\"}"))
                .uri(URI.create("http://localhost:8080/postish"))
                .header("Content-Type", "application/json")
                .build();
        sendOrDie(badRequest, client);

        logger.warn("NPE WITH WEB");

        final var npeRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/npe"))
                .build();
        sendOrDie(npeRequest, client);

        logger.warn("EXCEPTIONS");

        // Show stack trace logging
        final var e = new CoderMalfunctionError(
                new NullPointerException("OH MY, A NULL POINTER!"));
        logger.error("And I fail: {}", e.getMessage(), e);

        logger.warn("FEIGN");

        logger.warn("CALL OURSELVES WITH FEIGN DIRECT");

        logger.info("{}", loggy.getDirect());

        logger.warn("AND NOW FOR SOMETHING COMPLETELY POSTISH");

        loggy.post(new LoggyRequest(2, List.of(
                new Rolly(LocalDate.of(9876, 5, 4)))));

        logger.warn("CALL OURSELVES WITH FEIGN THROUGH FEIGN");

        logger.info("{}", loggy.getIndirect());

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

        logger.warn("CONFLICT WITH FEIGN");

        try {
            loggy.postConflict();
        } catch (final FeignException ignored) {
            // Already logged by logbook-feign logger
        }

        logger.warn("CONSTRAINT VIOLATION WITH FEIGN");

        try {
            loggy.post(new LoggyRequest(-1, List.of(
                    new Rolly(LocalDate.of(9876, 5, 4)))));
        } catch (final FeignException violation) {
            logger.error("Feign displeased: {}: {}",
                    getMostSpecificCause(violation).toString(), violation);
        }

        logger.warn("WEB + FEIGN");

        logger.warn("NOT FOUND WITH FEIGN THROUGH WEB");

        final var notFoundRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/not-found"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "abcdef0987654321",
                        "X-B3-SpanId", "abcdef0987654321")
                .build();

        sendOrDie(notFoundRequest, client);

        logger.warn("UNKNOWN HOST WITH FEIGN THROUGH WEB");

        final var unknownHostRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/unknown-host"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "bbcdef0987654321",
                        "X-B3-SpanId", "bbcdef0987654321")
                .build();

        sendOrDie(unknownHostRequest, client);

        logger.warn("CONFLICT WITH FEIGN THROUGH WEB");

        final var conflictRequest = HttpRequest.newBuilder()
                .POST(noBody())
                .uri(URI.create("http://localhost:8080/conflict"))
                .headers(
                        "X-B3-TraceId", "cbcdef0987654321",
                        "X-B3-SpanId", "cbcdef0987654321")
                .build();

        sendOrDie(conflictRequest, client);

        logger.warn("RETRY WITH FEIGN THROUGH WEB");

        final var retryRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/retry"))
                .build();

        sendOrDie(retryRequest, client);

        logger.warn("BUT IT'S ALRIGHT, IT'S OK, I'M GONNA RUN THAT WAY");

        logger.warn("PONG BUT NOT REQUEST/RESSPONSE LOGGING");

        sendOrDie(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/ping"))
                .build(), client);

        sendOrDie(HttpRequest.newBuilder()
                .POST(BodyPublishers
                        .ofString("{\"configuredLevel\":\"OFF\"}"))
                .uri(URI.create(format(
                        "http://localhost:8080/actuator/loggers/%s",
                        getClass().getName())))
                .header("Content-Type", "application/json")
                .build(), client);

        logger.error("I AM INVISIBLE, NO REALLY");
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
