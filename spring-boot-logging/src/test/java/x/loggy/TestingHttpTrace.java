package x.loggy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static x.loggy.HttpTrace.httpTraceOf;

@UtilityClass
public class TestingHttpTrace {
    public static Stream<String> httpHeaderTracesOf(
            final Logger httpLogger, final ObjectMapper objectMapper,
            final String headerName) {
        return httpTracesOf(httpLogger, objectMapper)
                .map(HttpTrace::getHeaders)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .filter(header -> header.getKey()
                        .equalsIgnoreCase(headerName))
                .map(Entry::getValue)
                .flatMap(Collection::stream);
    }

    public static Stream<HttpTrace> httpTracesOf(
            final Logger httpLogger, final ObjectMapper objectMapper) {
        final var captured = ArgumentCaptor.forClass(String.class);
        verify(httpLogger, atLeast(1)).trace(captured.capture());

        return captured.getAllValues().stream()
                .map(message -> httpTraceOf(objectMapper, message));
    }
}
