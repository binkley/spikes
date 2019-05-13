package x.loggy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TraceTesting {
    private final Logger httpLogger;
    private final ObjectMapper objectMapper;

    void assertExchange(
            final String existingTraceId, final boolean startsRemote) {
        if (null == existingTraceId)
            new AssertionSetup(startsRemote).assertExchange();
        else
            new AssertionSetup(existingTraceId, startsRemote)
                    .assertExchange();
    }

    @Value
    private static final class HttpTrace {
        String origin;
        Map<String, List<String>> headers;
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
            final int at = beginAssertingTraceIdAt(
                    startsRemote, allLogMessages);

            expectedTraceId = traceIdOf(httpTraceOf(
                    allLogMessages.get(at)));
            logMessages = allLogMessages.subList(
                    at + 1, allLogMessages.size());
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
                    "Wrong X-B3-TraceId header;"
                            + "%nExpected: <%s> but was:<%s>",
                    expectedTraceId, traceId)
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
                        .withFailMessage("Wrong origin;"
                                + "%nExpected: <remote> but was:<local>")
                        .isEqualTo("remote");
            else
                assertThat(trace.getOrigin())
                        .withFailMessage("Wrong origin;"
                                + "%nExpected: <local> but was:<remote>")
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
