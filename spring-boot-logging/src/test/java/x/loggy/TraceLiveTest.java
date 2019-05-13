package x.loggy;

import brave.Tracing;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Clock;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ActiveProfiles("json")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(properties = {
        "loggy.enable-demo=false"
}, webEnvironment = DEFINED_PORT)
class TraceLiveTest {
    private static final String traceId = "abcdef0987654321";

    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;
    private final UnknownHostRemote unknownHost;
    private final Clock clock;
    private final Tracing tracing;

    private final TracingTesting tracingTesting;

    @MockBean(name = "logger")
    private Logger logger;
    @MockBean(name = "httpLogger")
    private Logger httpLogger;

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
    void shouldTraceDirectlyIfClientProvides()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/direct"))
                .headers(
                        "X-B3-TraceId", traceId,
                        "X-B3-SpanId", traceId,
                        "X-B3-ParentSpanId", traceId)
                .build();
        final var client = HttpClient.newBuilder()
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        final var extractor = tracing.propagation()
                .extractor((HttpHeaders h, String key) ->
                        h.firstValue(key).orElse(null));
        final var extraction = extractor.extract(response.headers());

        assertThat(extraction.context().traceIdString())
                .isEqualTo(traceId);

        tracingTesting.assertExchange(traceId, true);
    }

    @Test
    void shouldTraceDirectlyIfClientOmits()
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

        tracingTesting.assertExchange(null, true);
    }

    @Test
    void shouldTraceIndirectlyIfClientProvides()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/indirect"))
                .headers(
                        "X-B3-TraceId", traceId,
                        "X-B3-SpanId", traceId,
                        "X-B3-ParentSpanId", traceId)
                .build();
        final var client = HttpClient.newBuilder()
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        final var extractor = tracing.propagation()
                .extractor((HttpHeaders h, String key) ->
                        h.firstValue(key).orElse(null));
        final var extraction = extractor.extract(response.headers());

        assertThat(extraction.context().traceIdString())
                .isEqualTo(traceId);

        tracingTesting.assertExchange(traceId, true);
    }

    @Test
    void shouldTraceIndirectlyIfClientOmits()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/indirect"))
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

        tracingTesting.assertExchange(null, true);
    }

    @Test
    void shouldTraceThroughFeignDirectIfClientProvides() {
        MDC.put("X-B3-TraceId", traceId);
        MDC.put("X-B3-SpanId", traceId);
        MDC.put("X-B3-ParentSpanId", traceId);

        final var response = loggy.getDirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));

        tracingTesting.assertExchange(traceId, false);
    }

    @Test
    void shouldTraceThroughFeignDirectIfClientOmits() {
        final var response = loggy.getDirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));

        tracingTesting.assertExchange(null, false);
    }

    @Test
    void shouldTraceThroughFeignIndirectIfClientProvides() {
        MDC.put("X-B3-TraceId", traceId);
        MDC.put("X-B3-SpanId", traceId);
        MDC.put("X-B3-ParentSpanId", traceId);

        final var response = loggy.getIndirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));

        tracingTesting.assertExchange(traceId, false);
    }

    @Test
    void shouldTraceThroughFeignIndirectIfClientOmits() {
        final var response = loggy.getIndirect();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));

        tracingTesting.assertExchange(null, false);
    }

    @Test
    void shouldHandleNotFoundIfClientProvides() {
        MDC.put("X-B3-TraceId", traceId);
        MDC.put("X-B3-SpanId", traceId);
        MDC.put("X-B3-ParentSpanId", traceId);

        assertThatThrownBy(notFound::get)
                .hasFieldOrPropertyWithValue("status", 404);

        tracingTesting.assertExchange(traceId, false);
    }

    @Test
    void shouldHandleNotFoundIfClientOmits() {
        assertThatThrownBy(notFound::get)
                .hasFieldOrPropertyWithValue("status", 404);

        tracingTesting.assertExchange(null, false);
    }

    @Test
    void shouldHandleUnknownHostIfClientProvides() {
        MDC.put("X-B3-TraceId", traceId);
        MDC.put("X-B3-SpanId", traceId);
        MDC.put("X-B3-ParentSpanId", traceId);

        assertThatThrownBy(unknownHost::get)
                .hasFieldOrPropertyWithValue("status", 0);

        tracingTesting.assertExchange(traceId, false);
    }

    @Test
    void shouldHandleUnknownHostIfClientOmits() {
        assertThatThrownBy(unknownHost::get)
                .hasFieldOrPropertyWithValue("status", 0);

        tracingTesting.assertExchange(null, false);
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
