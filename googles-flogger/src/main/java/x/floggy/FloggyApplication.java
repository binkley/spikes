package x.floggy;

import com.google.common.flogger.FluentLogger;

import static java.util.logging.Level.INFO;

public class FloggyApplication {
    public static void main(final String... args) {
        final var logger = FluentLogger.forEnclosingClass();

        logger.at(INFO).withCause(new NullPointerException("NULL!"))
                .log("Well, Bob.");
    }
}
