package x.loggy;

import brave.Tracing;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * @todo Messy helper methods
 * @todo Extract helper assertions to separate utility class
 */
@ActiveProfiles("json")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(properties = {
        "loggy.enable-demo=false"
}, webEnvironment = DEFINED_PORT)
class TraceLiveTest {
    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;
    private final Clock clock;
    private final Tracing tracing;
    private final ObjectMapper objectMapper;

    @MockBean(name = "logger")
    private Logger logger;
    @MockBean(name = "httpLogger")
    private Logger httpLogger;

    @BeforeEach
    void setUp() {
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
    void shouldTraceIfClientProvides()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .headers(
                        "X-B3-TraceId", "abcdef0987654321",
                        "X-B3-SpanId", "abcdef0987654321",
                        "X-B3-ParentSpanId", "abcdef0987654321")
                .build();
        final var client = HttpClient.newBuilder()
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        final var extractor = tracing.propagation()
                .extractor((HttpHeaders h, String key) ->
                        h.firstValue(key).orElse(null));
        final var extraction = extractor.extract(response.headers());

        assertThat(extraction.context().traceIdString())
                .isEqualTo("abcdef0987654321");

        assertThatAllContainsTraceIdInLogging("abcdef0987654321");
    }

    private void assertThatAllContainsTraceIdInLogging(final String traceId) {
        final var traces = ArgumentCaptor.forClass(String.class);
        verify(httpLogger, atLeast(1)).trace(traces.capture());

        final var allTraces = traces.getAllValues();

        allTraces.forEach(it ->
                assertThat(findTraceId(it)).isEqualTo(traceId));
    }

    private String findTraceId(final String logMessage) {
        try {
            return findTraceIdHeader(logMessage)
                    .map(Entry::getValue)
                    .map(values -> {
                        assertThat(values)
                                .withFailMessage(
                                        "Malformed X-B3-TraceId header")
                                .hasSize(1);
                        return values.get(0);
                    })
                    .orElseThrow((Supplier<AssertionError>) () -> fail(
                            "No X-B3-TraceId header"));
        } catch (final IOException e) {
            throw new IOError(e);
        }
    }

    private Optional<Entry<String, List<String>>> findTraceIdHeader(
            final String logMessage)
            throws IOException {
        return objectMapper.readValue(logMessage, HttpTrace.class)
                .getHeaders().entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase("x-b3-traceid"))
                .findFirst();
    }

    @Test
    void shouldTraceIfClientOmits()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .build();
        final var client = HttpClient.newBuilder()
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        final var extractor = tracing.propagation()
                .extractor((HttpHeaders h, String key) ->
                        h.firstValue(key).orElse(null));
        final var extraction = extractor.extract(response.headers());

        assertThat(extraction.context().traceIdString())
                .withFailMessage("Missing X-B3-TraceId header")
                .isNotNull();

        assertThatSubsequentContainsTraceIdInLogging();
    }

    private void assertThatSubsequentContainsTraceIdInLogging() {
        final var traces = ArgumentCaptor.forClass(String.class);
        verify(httpLogger, atLeast(1)).trace(traces.capture());

        final var allTraces = traces.getAllValues();

        assertThat(allTraces)
                .withFailMessage("No request/response pair")
                .hasSizeGreaterThanOrEqualTo(2);

        assertThatNoTraceId(allTraces.get(0));

        final var generated = findTraceId(allTraces.get(1));

        allTraces.subList(2, allTraces.size()).forEach(it ->
                assertThat(findTraceId(it)).isEqualTo(generated));
    }

    private void assertThatNoTraceId(final String logMessage) {
        try {
            findTraceIdHeader(logMessage).ifPresent(it ->
                    fail("Unexpected X-B3-TraceId header"));
        } catch (final IOException e) {
            throw new IOError(e);
        }
    }

    @Test
    void shouldSendTracingThroughFeignDirect() {
        MDC.put("X-B3-TraceId", "abcdef0987654321");
        MDC.put("X-B3-SpanId", "abcdef0987654321");
        MDC.put("X-B3-ParentSpanId", "abcdef0987654321");

        final var response = loggy.getDirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));
    }

    @Test
    void shouldSendTracingThroughFeignIndirect() {
        MDC.put("X-B3-TraceId", "abcdef0987654321");
        MDC.put("X-B3-SpanId", "abcdef0987654321");
        MDC.put("X-B3-ParentSpanId", "abcdef0987654321");

        final var response = loggy.getIndirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));
    }

    @Test
    void shouldHandleNotFound() {
        MDC.put("X-B3-TraceId", "abcdef0987654321");
        MDC.put("X-B3-SpanId", "abcdef0987654321");
        MDC.put("X-B3-ParentSpanId", "abcdef0987654321");

        assertThatThrownBy(notFound::get)
                .hasFieldOrPropertyWithValue("status", 404);
    }

    @TestConfiguration
    public static class MyTestConfiguration {

        @Bean
        @Primary
        public Clock testClock() {
            return Clock.fixed(Instant.ofEpochSecond(1_000_000L), UTC);
        }
    }

    @Value
    private static final class HttpTrace {
        String origin;
        Map<String, List<String>> headers;
    }
}
