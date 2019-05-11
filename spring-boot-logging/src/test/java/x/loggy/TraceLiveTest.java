package x.loggy;

import brave.Tracing;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(properties = {
        "loggy.enable-demo=false"
}, webEnvironment = DEFINED_PORT)
class TraceLiveTest {
    private final Clock clock;
    private final Tracing tracing;
    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;

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
}
