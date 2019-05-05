package x.loggy;

import org.slf4j.LoggerFactory;

public class LoggyApplication {
    public static void main(final String... args) {
        final var logger = LoggerFactory.getLogger(LoggyApplication.class);
        logger.info("Hi, mom!");
        logger.debug("Should not appear");
    }
}
