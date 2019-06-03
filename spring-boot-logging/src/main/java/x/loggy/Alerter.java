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

    public void alert(final AlertMessage alertMessage,
            final int status, final String method, final String url) {
        alert(alertMessage.message(), alertMessage.severity(),
                status, method, url);
    }

    public void alertLow(final String message) {
        alert(message, LOW, 0, null, null);
    }

    public void alertMedium(final String message) {
        alert(message, MEDIUM, 0, null, null);
    }

    public void alertHigh(final String message) {
        alert(message, HIGH, 0, null, null);
    }

    private void alert(final String message, final Severity severity,
            final int status, final String method, final String url) {
        if (null == method)
            logger.error("ALERT {}: {}", severity, message);
        else
            logger.error("ALERT {} [{} {} {}]: {}",
                    severity, status, method, url, message);
    }
}
