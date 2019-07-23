package x.loggy;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static x.loggy.TryLogbookMessageConverter.EMPTY_VALUE;

class TryLogbookMessageConverterTest {
    @Test
    void shouldConvertEventMessageWithPlainTextAndNoMessageArguments() {
        final var rawMessage = "TEXT MESSAGE WITHOUT SUBSTITUTION";

        runTest("FANCY", "text", rawMessage, rawMessage);
    }

    private void runTest(
            final String loggerName,
            final String property,
            final String value,
            final String rawMessage,
            final Object... messageArgs) {
        final var event = new LoggingEvent();
        event.setLoggerName(loggerName);
        event.setMessage(rawMessage);
        event.setArgumentArray(messageArgs);

        final var converter = new TryLogbookMessageConverter();
        converter.setOptionList(List.of(property));
        converter.start();

        final var converted = converter.convert(event);

        converter.stop();

        assertThat(converted).isEqualTo(String.valueOf(value));
    }

    @Test
    void shouldConvertEventMessageWithPlainTextAndWithMessageArguments() {
        runTest("FANCY", "text", "TEXT MESSAGE WITH SUBSTITUTION",
                "TEXT MESSAGE {}", "WITH SUBSTITUTION");
    }

    @Test
    void shouldConvertEventMessageAsJsonOnlyForRequestResponseLogging() {
        final var jsonMessage = "{\"a\":3}";

        runTest(Logbook.class.getName(), "json", jsonMessage,
                jsonMessage);
    }

    @Test
    void shouldConvertToEmptyStringWhenPlainTextLoggingIsForJson() {
        runTest("FANCY", "json", EMPTY_VALUE, "PLAIN TEXT");
    }

    @Test
    void shouldConvertToEmptyStringWhenRequestResponseLoggingIsForPlainText() {
        final var jsonMessage = "{\"a\":3}";

        runTest(Logbook.class.getName(), "text", EMPTY_VALUE,
                jsonMessage);
    }
}
