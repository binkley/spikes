package x.loggy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static x.loggy.HttpTrace.httpTracesOf;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AssertionsForTracingLogs {
    private final Logger httpLogger;
    private final ObjectMapper objectMapper;

    /**
     * Asserts that: <ol>
     * <li>Calls between local/remote exchange back and forth, with the first
     * expecting <var>startsRemote</var>.  Web clients should start
     * <em>remote</em> ({@code true}); Feign clients should start
     * <em>local</em> ({@code false}).</li>
     * <li>All calls, including the first, use an <var>existingTraceId</var>
     * for the {@code X-B3-TraceId} header, if provided.  This simulates a
     * client passing in an existing {@code X-B3-TraceId} header.</li>
     * <li>All calls after the first and beginning with the second use the
     * same generated trace ID for the {@code X-B3-TraceId} header, if no
     * <var>existingTraceId</var> is provided.  This simulates a client
     * omitting the {@code X-B3-TraceId} header, the first call originating in
     * the service.  The first call should have no {@code X-B3-TraceId}
     * header.</li>
     * <li>No call has multiple values for the {@code X-B3-TraceId}
     * header, and the header is <em>required</em> when a call should have a
     * trace ID.</li>
     * </ol>
     * <p>
     * Assumes that Logback logging is in JSON format so that it may be
     * asserted against without recourse to string parsing and regular
     * expressions.  In this project, use {@code @ActiveProfiles("json")} on
     * the test class.
     */
    public void assertExchange(
            final String existingTraceId, final boolean startsRemote) {
        final var setup = null == existingTraceId
                ? new AssertionSetup(startsRemote)
                : new AssertionSetup(existingTraceId, startsRemote);
        setup.assertExchange();
    }

    private final class AssertionSetup {
        private final AtomicBoolean remoteOrLocal;
        private final String expectedTraceId;
        private final List<HttpTrace> traces;

        private AssertionSetup(
                final String existingTraceId, final boolean startsRemote) {
            remoteOrLocal = new AtomicBoolean(startsRemote);
            expectedTraceId = existingTraceId;
            traces = httpTraces();
        }

        private List<HttpTrace> httpTraces() {
            return httpTracesOf(httpLogger, objectMapper)
                    .peek(this::assertOrigin)
                    .collect(toUnmodifiableList());
        }

        private void assertOrigin(final HttpTrace trace) {
            final var remote = remoteOrLocal.get();

            if (remote)
                assertThat(trace.getOrigin())
                        .withFailMessage("Wrong origin")
                        .isEqualTo("remote");
            else
                assertThat(trace.getOrigin())
                        .withFailMessage("Wrong origin")
                        .isEqualTo("local");

            remoteOrLocal.set(!remote);
        }

        private AssertionSetup(final boolean startsRemote) {
            remoteOrLocal = new AtomicBoolean(startsRemote);

            final List<HttpTrace> traces = httpTraces();
            final int beginAssertingTraceIdAt = beginAssertingTraceIdAt(
                    startsRemote, traces);

            expectedTraceId = traceIdOf(traces.get(beginAssertingTraceIdAt));
            this.traces = traces.subList(
                    beginAssertingTraceIdAt + 1, traces.size());
        }

        private int beginAssertingTraceIdAt(final boolean startsRemote,
                final List<HttpTrace> traces) {
            if (!startsRemote)
                return 0;

            final var firstTrace = traces.get(0);
            if (maybeTraceHeader(firstTrace).isPresent())
                fail("Unexpected X-B3-TraceId header in first log message");

            return 1;
        }

        private String traceIdOf(final HttpTrace trace) {
            return maybeTraceHeader(trace)
                    .map(Entry::getValue)
                    .map(this::firstOf)
                    .orElseThrow(() -> new AssertionError(
                            "Missing X-B3-TraceId header"));
        }

        private Optional<Entry<String, List<String>>> maybeTraceHeader(
                final HttpTrace trace) {
            return trace.getHeaders().entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase("x-b3-traceid"))
                    .findFirst();
        }

        private String firstOf(final List<String> values) {
            assertThat(values)
                    .withFailMessage("Malformed X-B3-TraceId header")
                    .hasSize(1);
            return values.get(0);
        }

        private void assertExchange() {
            traces.forEach(this::assertTraceId);
        }

        private void assertTraceId(final HttpTrace trace) {
            final var traceId = traceIdOf(trace);

            assertThat(traceId).withFailMessage(
                    "Wrong X-B3-TraceId header")
                    .isEqualTo(expectedTraceId);
        }
    }
}
