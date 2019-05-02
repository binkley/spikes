package com.example.demo;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import io.smartup.localstack.EnableLocalStack;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@EnableLocalStack
@SpringBootApplication
public class DemoApplication {
    private static final String queueName = "foos";
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(final String... args)
            throws InterruptedException {
        try (final var context = SpringApplication.run(
                DemoApplication.class, args)) {
            context.getBean(Pub.class).run();
            latch.await();
        }
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
