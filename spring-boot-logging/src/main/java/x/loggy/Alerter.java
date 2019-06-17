package x.loggy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import x.loggy.AlertMessage.Severity;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static x.loggy.AlertMessage.Severity.HIGH;
import static x.loggy.AlertMessage.Severity.LOW;
import static x.loggy.AlertMessage.Severity.MEDIUM;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Alerter {
    private final Logger logger;

    public void alert(final AlertMessage alertMessage,
            final Map<String, Object> extra) {
        alert(alertMessage.message(), alertMessage.severity(), extra);
    }

    private void alert(final String message, final Severity severity,
            final Map<String, Object> extra) {
        final var append = extra.entrySet().stream()
                .map(e -> format("%s=%s", e.getKey(), e.getValue()))
                .collect(joining(";"));
        if (append.isEmpty())
            logger.error("ALERT {}: {}", severity, message);
        else
            logger.error("ALERT {}: {};{}", severity, message, append);
    }

    public void alertLow(final String message) {
        alert(message, LOW, emptyMap());
    }

    public void alertMedium(final String message) {
        alert(message, MEDIUM, emptyMap());
    }

    public void alertHigh(final String message) {
        alert(message, HIGH, emptyMap());
    }
}
