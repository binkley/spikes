package x.loggy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import x.loggy.AlertMessage.Severity;

import static x.loggy.AlertMessage.Severity.HIGH;
import static x.loggy.AlertMessage.Severity.LOW;
import static x.loggy.AlertMessage.Severity.MEDIUM;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Alerter {
    private final Logger logger;

    public void alert(final AlertMessage alertMessage) {
        alert(alertMessage.message(), alertMessage.severity());
    }

    public void alertLow(final String message) {
        alert(message, LOW);
    }

    public void alertMedium(final String message) {
        alert(message, MEDIUM);
    }

    public void alertHigh(final String message) {
        alert(message, HIGH);
    }

    private void alert(final String message, final Severity severity) {
        logger.error("ALERT {}: {}", severity, message);
    }
}
