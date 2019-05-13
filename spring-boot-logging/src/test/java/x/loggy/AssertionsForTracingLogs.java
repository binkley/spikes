package x.loggy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

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
        private final List<String> logMessages;

        private AssertionSetup(
                final String existingTraceId, final boolean startsRemote) {
            remoteOrLocal = new AtomicBoolean(startsRemote);
            expectedTraceId = existingTraceId;
            logMessages = allLogMessages();
        }

        private List<String> allLogMessages() {
            final var captured = ArgumentCaptor.forClass(String.class);
            verify(httpLogger, atLeast(1)).trace(captured.capture());
            return captured.getAllValues();
        }

        private AssertionSetup(final boolean startsRemote) {
            remoteOrLocal = new AtomicBoolean(startsRemote);

            final var allLogMessages = allLogMessages();
            final int beginAssertingTraceIdAt = beginAssertingTraceIdAt(
                    startsRemote, allLogMessages);

            expectedTraceId = traceIdOf(httpTraceOf(
                    allLogMessages.get(beginAssertingTraceIdAt)));
            logMessages = allLogMessages.subList(
                    beginAssertingTraceIdAt + 1, allLogMessages.size());
        }

        private int beginAssertingTraceIdAt(final boolean startsRemote,
                final List<String> allLogMessages) {
            if (!startsRemote)
                return 0;

            final var firstTrace = httpTraceOf(allLogMessages.get(0));
            if (maybeTraceHeader(firstTrace).isPresent())
                fail("Unexpected X-B3-TraceId header in first log message");

            return 1;
        }

        private void assertExchange() {
            logMessages.forEach(it -> assertTraceId(httpTraceOf(it)));
        }

        private void assertTraceId(final HttpTrace trace) {
            final var traceId = traceIdOf(trace);

            assertThat(traceId).withFailMessage(
                    "Wrong X-B3-TraceId header")
                    .isEqualTo(expectedTraceId);
        }

        private HttpTrace httpTraceOf(final String logMessage) {
            try {
                final var trace = objectMapper.readValue(
                        logMessage, HttpTrace.class);
                assertOrigin(trace);
                return trace;
            } catch (final IOException e) {
                throw new IOError(e);
            }
        }

        private String traceIdOf(final HttpTrace trace) {
            return maybeTraceHeader(trace)
                    .map(Entry::getValue)
                    .map(this::firstOf)
                    .orElseThrow(() -> new AssertionError(
                            "Missing X-B3-TraceId header"));
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
    }
}
