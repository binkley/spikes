package x.spike;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableJms
public class SpikeApplication {
    public static void main(final String... args) {
        final var context = run(SpikeApplication.class, args);
        getLogger(SpikeApplication.class).info("MAIN");
        context.getBean(JmsTemplate.class).convertAndSend("BOB", "NANCY");
    }

    @JmsListener(destination = "BOB")
    public void getId(@Payload final String payload,
            @Headers final Map<String, String> headers) {
        getLogger(SpikeApplication.class).info("{}", payload);
        getLogger(SpikeApplication.class).info("{}", headers);
    }
}
