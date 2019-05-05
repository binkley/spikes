package x.loggy;

import org.slf4j.LoggerFactory;

import static x.loggy.SeverityConverter.HIGH;
import static x.loggy.SeverityConverter.LOW;
import static x.loggy.SeverityConverter.MEDIUM;
import static x.loggy.SeverityConverter.URGENT;

public class LoggyApplication {
    public static void main(final String... args) {
        final var logger = LoggerFactory.getLogger(LoggyApplication.class);
        logger.error("I cannot do that, Dave");
        logger.warn("Nanites nearby");
        logger.info("Happy, snappy");
        logger.debug("Should appear as regular logging, not as severity");

        logger.error(URGENT, "Bachman overdrive");
        logger.error(HIGH, "Turner overturned");
        logger.error(MEDIUM, "Flip to the B side");
        logger.error(LOW, "Sodium");
    }
}
