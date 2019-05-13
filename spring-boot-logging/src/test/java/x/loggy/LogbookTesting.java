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
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LogbookTesting {
    private final Logger httpLogger;
    private final ObjectMapper objectMapper;

    void assertExchange(
            final String firstTraceId, final boolean startsRemote) {
        if (null == firstTraceId)
            new AssertionSetup(startsRemote).assertExchange();
        else
            new AssertionSetup(firstTraceId, startsRemote).assertExchange();
    }

    @Value
    private static final class HttpTrace {
        String origin;
        Map<String, List<String>> headers;
    }

    private final class AssertionSetup {
        private final AtomicBoolean remoteOrLocal;
        private final String traceId;
        private final List<String> logMessages;

        private AssertionSetup(
                final String firstTraceId, final boolean startsRemote) {
            remoteOrLocal = new AtomicBoolean(startsRemote);
            traceId = firstTraceId;
            logMessages = allLogMessages();
        }

        private List<String> allLogMessages() {
            final var captured = ArgumentCaptor.forClass(String.class);
            verify(httpLogger, atLeast(2)).trace(captured.capture());
            return captured.getAllValues();
        }

        private AssertionSetup(final boolean startsRemote) {
            remoteOrLocal = new AtomicBoolean(startsRemote);

            final var allLogMessages = allLogMessages();

            final var firstTrace = httpTraceOf(allLogMessages.get(0));
            if (maybeTraceHeader(firstTrace).isPresent()) {
                fail("Unexpected X-B3-TraceId header");
            }

            final var secondTrace = httpTraceOf(allLogMessages.get(1));
            traceId = traceIdOf(secondTrace);
            logMessages = allLogMessages.subList(2, allLogMessages.size());
        }

        private HttpTrace httpTraceOf(final String logMessage) {
            try {
                return objectMapper.readValue(logMessage, HttpTrace.class);
            } catch (final IOException e) {
                throw new IOError(e);
            }
        }

        private Optional<Entry<String, List<String>>> maybeTraceHeader(
                final HttpTrace trace) {
            return trace.getHeaders().entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase("x-b3-traceid"))
                    .findFirst();
        }

        private String traceIdOf(final HttpTrace trace) {
            return maybeTraceHeader(trace)
                    .map(Entry::getValue)
                    .map(values -> {
                        assertThat(values).withFailMessage(
                                "Malformed X-B3-TraceId header")
                                .hasSize(1);
                        return values.get(0);
                    })
                    .orElseThrow((Supplier<AssertionError>) () -> fail(
                            "Missing X-B3-TraceId header"));
        }

        private void assertExchange() {
            for (final var logMessage : logMessages) {
                final var trace = httpTraceOf(logMessage);
                final var traceId = traceIdOf(trace);

                assertThat(traceId)
                        .withFailMessage("Wrong X-B3-TraceId header")
                        .isEqualTo(this.traceId);

                final var remote = remoteOrLocal.get();
                if (remote) {
                    assertThat(trace.getOrigin()).isEqualTo("remote");
                } else {
                    assertThat(trace.getOrigin()).isEqualTo("local");
                }
                remoteOrLocal.set(!remote);
            }
        }
    }
}
