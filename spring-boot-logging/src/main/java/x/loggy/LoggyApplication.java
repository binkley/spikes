package x.loggy;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import feign.FeignException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CountDownLatch;

import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootApplication
public class LoggyApplication
        implements CommandLineRunner {
    private static final String queueName = "foos";
    private static final CountDownLatch latch = new CountDownLatch(1);

    private final SampleHttpBin happyPath;
    private final NowheresVille sadPath;
    private final NotAThing notFound;
    private final Logger logger;

    public static void main(final String... args)
            throws InterruptedException {
        // FYI -- using the try-block shuts down the program after
        // the command-line runner finishes: Faster feedback cycle
        try (final var context = SpringApplication
                .run(LoggyApplication.class, args)) {
            context.getBean(Pub.class).run();
            latch.await();
        }
    }

    @Override
    public void run(final String... args)
            throws IOException, InterruptedException {
        logger.info("I am in COMMAND");
        logger.debug("And this is json: {\"a\":3}"); // Logged as string
        logger.debug("{\"a\":3}"); // Logged as embedded JSON, not string

        // Talk to ourselves
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080"))
                .build();
        final var client = HttpClient.newBuilder()
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        logger.debug("(Really got {} after sending {})", response, request);
        logger.info("{}", response.body());

        // Show stack trace logging
        final var e = new NullPointerException("OH MY, A NULL POINTER!");
        logger.error("And I fail: {}", e.getMessage(), e);

        // Feign
        final var happyFeign = happyPath.get();
        logger.info("He said, {}", happyFeign);

        try {
            sadPath.get();
        } catch (final FeignException ignored) {
            // Already logged by logbook-feign logger
        }

        try {
            notFound.get();
        } catch (final FeignException notFound) {
            logger.error("Feign angry: {}: {}",
                    getMostSpecificCause(notFound).toString(),
                    notFound.contentUTF8(),
                    notFound);
        }

        logger.info("BUT IT'S ALRIGHT, IT'S OK, I'M GONNA RUN THAT WAY");
    }

    @Component
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public static class Setup
            implements ApplicationListener<ApplicationReadyEvent> {
        private final AmazonSQSAsync sqs;
        private final Logger logger;

        @Override
        public void onApplicationEvent(final ApplicationReadyEvent event) {
            final var queue = sqs.createQueue(queueName);
            logger.debug("Purging queue");
            sqs.purgeQueue(new PurgeQueueRequest(queue.getQueueUrl()));
            logger.info("Queue purged");
        }
    }

    @Component
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public static class Pub
            implements Runnable {
        private final AmazonSQSAsync sqs;
        private final Logger logger;

        @Override
        public void run() {
            final var foo = new Foo();
            foo.number = 3;
            logger.debug("Sending a Foo! {}", foo);
            new QueueMessagingTemplate(sqs).convertAndSend(queueName, foo);
            logger.info("Sent a Foo! {}", foo);
        }
    }

    @Component
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public static class Sub {
        private final Logger logger;

        @SqsListener(queueName)
        public void receive(final Foo foo) {
            logger.info("Got a Foo! {}", foo);
            latch.countDown();
        }
    }

    @Data
    public static class Foo {
        public int number;
    }
}
