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

    void assertThatAllContainsTraceIdInLogging(final String traceId) {
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

    void assertThatSubsequentContainsTraceIdInLogging() {
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

    @Value
    private static final class HttpTrace {
        String origin;
        Map<String, List<String>> headers;
    }
}
