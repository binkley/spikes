package x.loggy;

import org.slf4j.LoggerFactory;

public class LoggyApplication {
    public static void main(final String... args) {
        LoggerFactory.getLogger(LoggyApplication.class)
                .info("Hi, mom!");
    }
}
