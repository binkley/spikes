package x.loggy;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggySqsListener {
    private final Logger logger;

    @SqsListener("foo")
    public void logSqsMessage(final FooMessage foo) {
        logger.info("SQS MESSAGE: {}", foo);
    }

    @Data
    public static class FooMessage {
        public String data;
    }
}
