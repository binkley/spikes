package x.loggy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.event.EventListener;
import org.springframework.core.style.ToStringCreator;
import org.springframework.stereotype.Component;
import x.loggy.LoggyRequest.Rolly;
import x.loggy.data.BobRecord;
import x.loggy.data.BobRepository;

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
import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static org.springframework.boot.logging.LogLevel.OFF;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@ConditionalOnProperty(prefix = "loggy", name = "enable-demo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggyDemo {
    private static final HttpClient client = newHttpClient();

    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;
    private final ServiceDownRemote serviceDown;
    private final UnknownHostRemote unknownHost;
    private final ObjectMapper objectMapper;
    private final Logger logger;
    private final TraceIdsStarter starter;
    private final BobRepository bobRepository;

    @EventListener
    public void ready(final ApplicationReadyEvent event)
            throws JsonProcessingException {
        starter.newTraceIdsOnCurrentThread();

        out.println();
        informUser("SIMPLE LOGGING");
        logger.info("I am ready: {}", new ToStringCreator(event)
                .append("args", event.getArgs())
                .append("source", event.getSource())
                .append("timestamp", event.getTimestamp()));
        logger.debug("And this is json: {\"a\":3}"); // Logged as string
        logger.debug("{\"a\":3}"); // Logged as embedded JSON, not string

        informUser("EXCEPTIONS");

        // Show stack trace logging
        final var e = new CoderMalfunctionError(
                new NullPointerException("OH MY, A NULL POINTER!"));
        logger.error("And I fail: {}", e.getMessage(), e);

        out.println();
        informUser("CALL OURSELVES");
        informUser("GET WITH WEB");
        informUser("INVALID INBOUND TRACE ID");

        final var invalidTracingRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "not-a-trace-id",
                        "X-B3-SpanId", "not-a-trace-id")
                .build();

        sendOrDie(invalidTracingRequest);

        informUser("VALID INBOUND TRACE ID");

        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "abcdef0987654321",
                        "X-B3-SpanId", "abcdef0987654321")
                .build();

        final HttpResponse<String> response = sendOrDie(request);

        logger.info("{}", response.body());
        logger.debug("(Really got {} after sending {})", response, request);

        informUser("POST WITH WEB");

        final var postRequest = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(
                        new LoggyRequest(2, List.of(
                                new Rolly(LocalDate.of(9876, 5, 4)))))))
                .uri(URI.create("http://localhost:8080/postish"))
                .header("Content-Type", "application/json")
                .build();
        sendOrDie(postRequest);

        informUser("CONSTRAINT VIOLATION WITH WEB");

        final var poorRequest = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(
                        new LoggyRequest(-1, List.of(
                                new Rolly(LocalDate.of(9876, 5, 4)))))))
                .uri(URI.create("http://localhost:8080/postish"))
                .header("Content-Type", "application/json")
                .build();
        sendOrDie(poorRequest);

        informUser("MISMATCHED INPUT WITH WEB");

        final var badRequest = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(
                        "{\"blinken-lights\":2,\"when\":\"not-a-date\"}"))
                .uri(URI.create("http://localhost:8080/postish"))
                .header("Content-Type", "application/json")
                .build();
        sendOrDie(badRequest);

        informUser("NPE WITH WEB");

        final var npeRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/npe"))
                .build();
        sendOrDie(npeRequest);

        out.println();
        informUser("FEIGN");
        informUser("CALL OURSELVES WITH FEIGN DIRECT");

        logger.info("{}", loggy.getDirect());

        informUser("AND NOW FOR SOMETHING COMPLETELY POSTISH");

        loggy.post(new LoggyRequest(2, List.of(
                new Rolly(LocalDate.of(9876, 5, 4)))));

        informUser("CALL OURSELVES WITH FEIGN THROUGH FEIGN");

        logger.info("{}", loggy.getIndirect());

        informUser("NOT FOUND");

        try {
            notFound.get();
        } catch (final FeignException notFound) {
            logger.error("Feign angry: {}: {}",
                    getMostSpecificCause(notFound),
                    notFound.contentUTF8(),
                    notFound);
        }

        informUser("SERVICE DOWN");

        try {
            serviceDown.get();
        } catch (final FeignException ignored) {
            // Already logged by logbook-feign logger
        }

        informUser("UNKNOWN HOST");

        try {
            unknownHost.get();
        } catch (final FeignException ignored) {
            // Already logged by logbook-feign logger
        }

        informUser("CONFLICT WITH FEIGN");

        try {
            loggy.postConflict();
        } catch (final FeignException ignored) {
            // Already logged by logbook-feign logger
        }

        informUser("CONSTRAINT VIOLATION WITH FEIGN");

        try {
            loggy.post(new LoggyRequest(-1, List.of(
                    new Rolly(LocalDate.of(9876, 5, 4)))));
        } catch (final FeignException violation) {
            logger.error("Feign displeased: {}: {}",
                    getMostSpecificCause(violation), violation);
        }

        out.println();
        informUser("WEB + FEIGN");
        informUser("NOT FOUND WITH FEIGN THROUGH WEB");

        final var notFoundRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/not-found"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "abcdef0987654321",
                        "X-B3-SpanId", "abcdef0987654321")
                .build();

        sendOrDie(notFoundRequest);

        informUser("SERVICE DOWN WITH FEIGN THROUGH WEB");

        final var serviceDownRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/service-down"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "abcdef0987654321",
                        "X-B3-SpanId", "abcdef0987654321")
                .build();

        sendOrDie(serviceDownRequest);

        informUser("UNKNOWN HOST WITH FEIGN THROUGH WEB");

        final var unknownHostRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/unknown-host"))
                .expectContinue(true)
                .headers(
                        "X-B3-TraceId", "bbcdef0987654321",
                        "X-B3-SpanId", "bbcdef0987654321")
                .build();

        sendOrDie(unknownHostRequest);

        informUser("CONFLICT WITH FEIGN THROUGH WEB");

        final var conflictRequest = HttpRequest.newBuilder()
                .POST(noBody())
                .uri(URI.create("http://localhost:8080/conflict"))
                .headers(
                        "X-B3-TraceId", "cbcdef0987654321",
                        "X-B3-SpanId", "cbcdef0987654321")
                .build();

        sendOrDie(conflictRequest);

        informUser("RETRY WITH FEIGN THROUGH WEB");

        final var retryRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/retry"))
                .build();

        sendOrDie(retryRequest);

        out.println();
        informUser("BUT IT'S ALRIGHT, IT'S OK, I'M GONNA RUN THAT WAY");
        informUser("PONG BUT NOT REQUEST/RESPONSE LOGGING (NO OUTPUT)");

        sendOrDie(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/ping"))
                .build());
        loggy.getPing();

        out.println();
        informUser("BOB, BOB, BOB");
        informUser("NO BOBS HERE");

        final var bobsRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/bobs"))
                .build();

        final var bobsResponse = sendOrDie(bobsRequest).body();

        logger.info("NO BOBS FOR YOU: {}", bobsResponse);

        informUser("MAKE A BOB");

        final var unsaved = new BobRecord();
        unsaved.name = "William";
        final var saved = bobRepository.save(unsaved);
        final var found = bobRepository.findById(saved.id).get();

        logger.info("{} THAT {} IS {}",
                saved.equals(found), found, saved);

        out.println();
        informUser("TURNING DOWN VOLUME ON LOGGER (NO OUTPUT)");

        adjustLogging(OFF);

        logger.error("I AM INVISIBLE, NO REALLY");

        adjustLogging(WARN);

        out.println();
        informUser("DONE!");
    }

    private void informUser(final String message) {
        logger.warn(message);
    }

    private HttpResponse<String> sendOrDie(final HttpRequest request) {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (final IOException e) {
            logger.error("SERVER NOT READY? {}",
                    getMostSpecificCause(e), e);
            throw new IOError(e);
        } catch (final InterruptedException e) {
            logger.error("INTERRUPTED? {}",
                    getMostSpecificCause(e), e);
            currentThread().interrupt();
            return null;
        }
    }

    private void adjustLogging(final LogLevel level)
            throws JsonProcessingException {
        sendOrDie(HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(objectMapper
                        .writeValueAsString(new AdjustLogging(level))))
                .uri(URI.create(format(
                        "http://localhost:8080/actuator/loggers/%s",
                        getClass().getName())))
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .build());
    }

    @Value
    private static class AdjustLogging {
        // TODO: Why no kebab-case in this annotation?
        @JsonProperty("configuredLevel")
        LogLevel configuredLevel;
    }
}
