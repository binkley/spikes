package x.loggy;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;
import x.loggy.HttpTrace.RequestTrace;
import x.loggy.HttpTrace.ResponseTrace;

import java.net.URI;
import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

class LogbookPropertyConverterTest {
    // NB -- This is *not* an injected class -- it works only for simple
    // fields, which is all
    // the kinds of fields had by HTTP traces
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final BiConsumer<HttpTrace, String> noop
            = (trace, value) -> { };

    @Test
    void shouldConvertCorrelationForRequests()
            throws
            JsonProcessingException {
        runTest(new RequestTrace(), "correlation", "abc123",
                HttpTrace::setCorrelation);
    }

    private <T extends HttpTrace, V> void runTest(final T trace,
            final String property, final V value,
            final BiConsumer<T, V> assignToTrace)
            throws JsonProcessingException {
        assignToTrace.accept(trace, value);

        final var event = new LoggingEvent();
        // TODO: Negative test for not converting for other logger classes
        event.setLoggerName(Logbook.class.getName());
        event.setMessage(objectMapper.writeValueAsString(trace));

        final var converter = new LogbookPropertyConverter();
        converter.setOptionList(List.of(property));
        converter.start();

        final var converted = converter.convert(event);

        converter.stop();

        assertThat(converted).isEqualTo(String.valueOf(value));
    }

    @Test
    void shouldConvertCorrelationForResponses()
            throws JsonProcessingException {
        runTest(new ResponseTrace(), "correlation", "abc123",
                HttpTrace::setCorrelation);
    }

    @Test
    void shouldConvertOriginForRequests()
            throws JsonProcessingException {
        runTest(new RequestTrace(), "origin", "local",
                HttpTrace::setOrigin);
    }

    @Test
    void shouldConvertOriginForResponses()
            throws JsonProcessingException {
        runTest(new ResponseTrace(), "origin", "local",
                HttpTrace::setOrigin);
    }

    @Test
    void shouldConvertMethodForRequests()
            throws JsonProcessingException {
        runTest(new RequestTrace(), "method", GET.name(),
                RequestTrace::setMethod);
    }

    @Test
    void shouldConvertMethodForResponses()
            throws JsonProcessingException {
        runTest(new ResponseTrace(), "method", "", noop);
    }

    @Test
    void shouldConvertUriPathForRequests()
            throws JsonProcessingException {
        runTest(new RequestTrace(), "uriPath", "/GET/BAR",
                (trace, value) -> trace
                        .setUri(URI.create("http://no.where" + value)));
    }

    @Test
    void shouldConvertUriPathForResponses()
            throws JsonProcessingException {
        runTest(new ResponseTrace(), "uriPath", "", noop);
    }

    @Test
    void shouldConvertStatusForRequests()
            throws JsonProcessingException {
        runTest(new RequestTrace(), "status", "", noop);
    }

    @Test
    void shouldConvertStatusForResponses()
            throws JsonProcessingException {
        runTest(new ResponseTrace(), "status", OK.value(),
                ResponseTrace::setStatus);
    }

    @Test
    void shouldConvertDurationForRequests()
            throws JsonProcessingException {
        runTest(new RequestTrace(), "duration", "", noop);
    }

    @Test
    void shouldConvertDurationForResponses()
            throws JsonProcessingException {
        runTest(new ResponseTrace(), "duration", 1200,
                ResponseTrace::setDuration);
    }

    @Test
    void shouldRaiseErrorWhenUnknownPropertyForRequests() {
        assertThatThrownBy(() ->
                runTest(new RequestTrace(), "FUNNY_PROPERTY", "", noop))
                .isInstanceOf(Bug.class);
    }

    @Test
    void shouldRaiseErrorWhenUnknownPropertyForResponses() {
        assertThatThrownBy(() ->
                runTest(new ResponseTrace(), "FUNNY_PROPERTY", "", noop))
                .isInstanceOf(Bug.class);
    }

    @Test
    void shouldConvertToEmptyStringForLoggerDifferentThatLogbook() {
        final var event = new LoggingEvent();
        event.setLoggerName("MY_CLASS");
        final var converter = new LogbookPropertyConverter();
        assertThat(converter.convert(event)).isEmpty();
    }
}
