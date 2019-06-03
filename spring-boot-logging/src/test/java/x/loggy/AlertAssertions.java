package x.loggy;

import lombok.experimental.UtilityClass;
import x.loggy.AlertMessage.Severity;

import static org.assertj.core.api.Assertions.assertThat;
import static x.loggy.AlertMessage.MessageFinder.findAlertMessage;

@UtilityClass
public class AlertAssertions {
    public static void assertThatAlertMessage(final Throwable t,
            final String message, final Severity severity) {
        final var alertMessage = findAlertMessage(t);
        assertThat(alertMessage).isNotNull();
        assertThat(alertMessage.message()).isEqualTo(message);
        assertThat(alertMessage.severity()).isEqualTo(severity);
    }
}
