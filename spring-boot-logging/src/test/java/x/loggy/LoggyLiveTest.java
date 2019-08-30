package x.loggy;

import brave.Tracing;
import brave.propagation.TraceContext.Extractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.zalando.problem.Problem;
import x.loggy.HttpTrace.RequestTrace;
import x.loggy.HttpTrace.ResponseTrace;
import x.loggy.LoggyRequest.Rolly;
import x.loggy.TestableConstraintViolationProblem.TestableViolation;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.time.LocalDate.now;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static x.loggy.AlertAssertions.assertThatAlertMessage;
import static x.loggy.AlertMessage.Severity.HIGH;
import static x.loggy.AlertMessage.Severity.MEDIUM;
import static x.loggy.LogbookBodyOnErrorsOnlyFilter.HIDDEN_BODY_AS_STRING;
import static x.loggy.TestingHttpTrace.httpHeaderTracesOf;
import static x.loggy.TestingHttpTrace.httpTracesOf;

@ActiveProfiles("json")
@AutoConfigureEmbeddedDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(properties = {
        "logging.level.x.loggy=WARN",
        "loggy.enable-demo=false"
}, webEnvironment = DEFINED_PORT)
@TestInstance(PER_CLASS)
class LoggyLiveTest {
    private static final String existingTraceId = "abcdef0987654321";
    private static final HttpClient client = newHttpClient();

    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;
    private final UnknownHostRemote unknownHost;
    private final ConflictRemote conflict;
    private final RetryRemote retry;
    private final Clock clock;
    private final Tracing tracing;
    private final AssertionsForTracingLogs tracingLogs;
    private final ObjectMapper objectMapper;
    private final TraceIdsStarter starter;

    @MockBean(name = "logger")
    private Logger logger;
    @MockBean(name = "httpLogger")
    private Logger httpLogger;

    private Extractor<HttpHeaders> httpExtractor;

    @PostConstruct
    private void init() {
        httpExtractor = tracing.propagation()
                .extractor((HttpHeaders h, String key) ->
                        h.firstValue(key).orElse(null));
    }

    @BeforeEach
    void setUp() {
        MDC.clear();

        lenient().when(logger.isTraceEnabled()).thenReturn(true);
        lenient().when(logger.isDebugEnabled()).thenReturn(true);
        lenient().when(logger.isInfoEnabled()).thenReturn(true);
        lenient().when(logger.isWarnEnabled()).thenReturn(true);
        lenient().when(logger.isErrorEnabled()).thenReturn(true);
        lenient().when(httpLogger.isTraceEnabled()).thenReturn(true);
        lenient().when(httpLogger.isDebugEnabled()).thenReturn(true);
        lenient().when(httpLogger.isInfoEnabled()).thenReturn(true);
        lenient().when(httpLogger.isWarnEnabled()).thenReturn(true);
        lenient().when(httpLogger.isErrorEnabled()).thenReturn(true);
    }

    @Test
    void givenExistingInvalidTrace_shouldWarn()
            throws IOException, InterruptedException {
        final var invalidTraceId = "not-a-trace-id";
        final var request = requestWithTracing(invalidTraceId)
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .build();

        client.send(request, discarding());

        // TODO: Less over-specific test w.r.t. arguments
        verify(logger).warn(anyString(),
                eq(invalidTraceId), eq(invalidTraceId),
                anyString(), anyString());
    }

    private static HttpRequest.Builder requestWithTracing(
            final String traceId) {
        return HttpRequest.newBuilder().headers(
                "X-B3-TraceId", traceId,
                "X-B3-SpanId", traceId);
    }

    @Test
    void givenExistingTrace_shouldTraceThoughWebDirectly()
            throws IOException, InterruptedException {
        final var request = requestWithTracing(existingTraceId)
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .build();

        final var traceId = sendAndExtractTraceId(request);

        assertThat(traceId).isEqualTo(existingTraceId);

        tracingLogs.assertExchange(existingTraceId, true);
    }

    private String sendAndExtractTraceId(final HttpRequest request)
            throws IOException, InterruptedException {
        final var response = sendAndDiscardBody(request);

        return extractTraceId(response);
    }

    private static HttpResponse<Void> sendAndDiscardBody(
            final HttpRequest request)
            throws IOException, InterruptedException {
        return client.send(request, discarding());
    }

    private String extractTraceId(final HttpResponse<Void> response) {
        return httpExtractor.extract(response.headers())
                .context()
                .traceIdString();
    }

    @Test
    void givenNoExistingTrace_shouldTraceThoughWebDirectly()
            throws IOException, InterruptedException {
        final var request = requestWithoutTracing()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .build();

        final var traceId = sendAndExtractTraceId(request);

        assertThat(traceId)
                .withFailMessage("Missing X-B3-TraceId header")
                .isNotNull();

        tracingLogs.assertExchange(null, true);
    }

    private static HttpRequest.Builder requestWithoutTracing() {
        return HttpRequest.newBuilder();
    }

    @Test
    void givenExistingTrace_shouldTraceThoughWebIndirectly()
            throws IOException, InterruptedException {
        final var request = requestWithTracing(existingTraceId)
                .GET()
                .uri(URI.create("http://localhost:8080/indirect"))
                .build();

        final var traceId = sendAndExtractTraceId(request);

        assertThat(traceId).isEqualTo(existingTraceId);

        tracingLogs.assertExchange(existingTraceId, true);
    }

    @Test
    void givenNoExistingTrace_shouldTraceThoughWebIndirectly()
            throws IOException, InterruptedException {
        final var request = requestWithoutTracing()
                .GET()
                .uri(URI.create("http://localhost:8080/indirect"))
                .build();

        final var traceId = sendAndExtractTraceId(request);

        assertThat(traceId)
                .withFailMessage("Missing X-B3-TraceId header")
                .isNotNull();

        tracingLogs.assertExchange(null, true);
    }

    @Test
    void givenExistingTrace_shouldTraceThroughWebConflictedly()
            throws IOException, InterruptedException {
        final var request = requestWithTracing(existingTraceId)
                .POST(noBody())
                .uri(URI.create("http://localhost:8080/conflict"))
                .build();

        final var response = sendAndDiscardBody(request);
        final var traceId = extractTraceId(response);

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(traceId).isEqualTo(existingTraceId);

        tracingLogs.assertExchange(existingTraceId, true);
    }

    @Test
    void givenExistingTrace_shouldTraceThroughFeignDirectly() {
        final var context = starter.newTraceIdsOnCurrentThread();

        final var response = loggy.getDirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));

        tracingLogs.assertExchange(context.traceIdString(), false);
    }

    @Test
    void givenNoExistingTrace_shouldTraceThroughFeignDirectly() {
        final var response = loggy.getDirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));

        tracingLogs.assertExchange(null, false);
    }

    @Test
    void givenExistingTrace_shouldTraceThroughFeignIndirectly() {
        final var context = starter.newTraceIdsOnCurrentThread();

        final var response = loggy.getIndirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));

        tracingLogs.assertExchange(context.traceIdString(), false);
    }

    @Test
    void givenNoExistingTrace_shouldTraceThroughFeignIndirectly() {
        final var response = loggy.getIndirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));

        tracingLogs.assertExchange(null, false);
    }

    @Test
    void givenExistingTrace_shouldHandleNotFound() {
        final var context = starter.newTraceIdsOnCurrentThread();

        assertThatThrownBy(notFound::get)
                .hasFieldOrPropertyWithValue("status", 404);

        tracingLogs.assertExchange(context.traceIdString(), false);
    }

    @Test
    void givenNoExistingTrace_shouldHandleNotFound() {
        assertThatThrownBy(notFound::get)
                .hasFieldOrPropertyWithValue("status", 404);

        tracingLogs.assertExchange(null, false);
    }

    @Test
    void givenFlakyRemote_shouldRetry() {
        retry.getRetry();

        tracingLogs.assertExchange(null, false);
    }

    @Test
    void givenExistingTrace_shouldHandleUnknownHost() {
        final var context = starter.newTraceIdsOnCurrentThread();

        assertThatThrownBy(unknownHost::get)
                .hasFieldOrPropertyWithValue("status", -1);

        tracingLogs.assertExchange(context.traceIdString(), false);
    }

    @Test
    void givenNoExistingTrace_shouldHandleUnknownHost() {
        assertThatThrownBy(unknownHost::get)
                .hasFieldOrPropertyWithValue("status", -1);

        final var traces = httpTracesOf(httpLogger, objectMapper)
                .collect(toUnmodifiableList());

        // Show retry in action; note each retry gets a fresh trace ID
        // since none was provided externally
        assertThat(traces)
                .extracting(HttpTrace::getOrigin)
                .containsExactly("local", "local");
        assertThat(traces.stream()
                .map(HttpTrace::getHeaders)
                .flatMap(h -> h.entrySet().stream())
                .filter(e -> e.getKey().equalsIgnoreCase("X-B3-TraceId"))
                .map(Map.Entry::getValue)
                .distinct())
                .hasSize(1);
    }

    @Test
    void shouldAlertThroughWebDirectly()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/npe"))
                .build();

        final var response = sendAndDiscardBody(request);

        assertThat(response.statusCode()).isEqualTo(500);

        verify(logger).error(anyString(), eq(HIGH), eq("NULLITY"),
                matches("code-exception=java\\.lang\\.NullPointerException:"
                        + " SAD, SAD"
                        + ";code-location=x\\.loggy\\.LoggyController"
                        + "\\.getNpe\\(LoggyController\\.java:\\d+\\)"
                        + ";response-status=500"
                        + ";request-method=GET"
                        + ";request-url=http://localhost:8080/npe"));

        assertThat(httpTracesOf(httpLogger, objectMapper)
                .filter(HttpTrace::isProblem)
                .map(trace -> trace.getBodyAs(Problem.class, objectMapper)))
                .allSatisfy(LoggyLiveTest::assertHasExtra);
    }

    private static void assertHasExtra(final Problem problem) {
        assertThat(problem.getParameters()).containsKeys(
                "code-exception",
                "code-location",
                "response-status",
                "request-method",
                "request-url");
    }

    @Test
    void shouldAlertThroughFeignIndirectlyOn5xx()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(noBody())
                .uri(URI.create("http://localhost:8080/conflict"))
                .build();

        final var response = sendAndDiscardBody(request);

        assertThat(response.statusCode()).isEqualTo(500);

        // TODO: Bad test
        verify(logger).error(anyString(), eq(MEDIUM), eq("CONFLICTED"),
                eq("code-exception=feign.FeignException$Conflict:"
                        + " status 409 reading ConflictRemote#postConflict();"
                        + "code-location=x.loggy.LoggyController.conflict"
                        + "(LoggyController.java:70);response-status=500;"
                        + "request-method=POST;"
                        + "request-url=http://localhost:8080/conflict"));
    }

    @Test
    void shouldAlertThroughFeignIndirectlyOnException()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/unknown-host"))
                .build();

        final var response = sendAndDiscardBody(request);

        assertThat(response.statusCode()).isEqualTo(BAD_GATEWAY.value());

        verify(logger).error(anyString(), eq(HIGH), eq("UNKNOWABLE HOST"),
                matches("code-exception=java\\.net\\.UnknownHostException:"
                        + " not\\.really\\.a\\.place"
                        + ";code-location=x\\.loggy\\.LoggyController"
                        + "\\.getUnknownHost\\(LoggyController\\.java:\\d+\\)"
                        + ";response-status=502"
                        + ";request-method=GET"
                        + ";request-url=http://localhost:8080/unknown-host"));
    }

    @Test
    void givenAlertDirectly() {
        assertThatThrownBy(conflict::postConflict)
                .isInstanceOf(FeignException.class)
                .satisfies(t -> assertThatAlertMessage(t,
                        "CONFLICTED", MEDIUM));
    }

    @Test
    void shouldObfuscateSecrets()
            throws IOException, InterruptedException {
        final var secret = "BOB BOB BOB";
        final var sensitive = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .header("X-Secret-Value", secret)
                .build();

        sendAndDiscardBody(sensitive);

        assertThat(httpHeaderTracesOf(httpLogger, objectMapper,
                "X-Secret-Value")
                .filter(value -> value.equals(secret))
                .findFirst())
                .isEmpty();
    }

    @Test
    void shouldNotLogRequestBodiesOnSuccess()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(
                        new LoggyRequest(3, List.of(new Rolly(now(clock)))))))
                .uri(URI.create("http://localhost:8080/postish"))
                .header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .build();

        sendAndDiscardBody(request);

        assertThat(httpTracesOf(httpLogger, objectMapper)
                .map(trace -> trace.getBodyAs(String.class, objectMapper)))
                .containsExactly(
                        HIDDEN_BODY_AS_STRING, HIDDEN_BODY_AS_STRING);
    }

    @Test
    void shouldLogResponseBodiesOnlyOnFailure()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(
                        new LoggyRequest(-3,
                                List.of(new Rolly(now(clock)))))))
                .uri(URI.create("http://localhost:8080/unpostish"))
                .header(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .build();

        sendAndDiscardBody(request);

        assertThat(httpTracesOf(httpLogger, objectMapper)
                .filter(RequestTrace.class::isInstance)
                .map(trace -> trace.getBodyAs(String.class, objectMapper)))
                .containsExactly(HIDDEN_BODY_AS_STRING);
        assertThat(httpTracesOf(httpLogger, objectMapper)
                .filter(ResponseTrace.class::isInstance)
                .map(trace -> trace.getBodyAs(
                        TestableConstraintViolationProblem.class,
                        objectMapper)))
                .containsExactly(TestableConstraintViolationProblem.builder()
                        .violation(TestableViolation.builder()
                                .field("blinkenLights")
                                .message("must be greater than or equal to 1")
                                .build())
                        .build());
    }

    @TestConfiguration
    public static class MyTestConfiguration {
        @Bean
        @Primary
        public Clock testClock() {
            return Clock.fixed(Instant.ofEpochSecond(1_000_000L), UTC);
        }
    }
}
