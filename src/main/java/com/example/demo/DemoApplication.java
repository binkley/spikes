package com.example.demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import io.smartup.localstack.EnableLocalStack;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;

@EnableLocalStack
@SpringBootApplication
public class DemoApplication {
    private static final String queueName = "foos";
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(final String... args)
            throws InterruptedException {
        System.setProperty("aws.accessKeyId", "foo");
        System.setProperty("aws.secretKey", "bar");

        try (final var context = SpringApplication
                .run(DemoApplication.class, args)) {
            latch.await();
        }
    }

    @Configuration
    public static class DemoConfiguration {
        @SuppressWarnings("unchecked")
        private static <T> T fieldOf(
                final Object target, final String fieldName) {
            final var field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            return (T) getField(field, target);
        }

        @Bean
        public AWSCredentialsProvider awsCredentialsProvider() {
            return new SystemPropertiesCredentialsProvider();
        }

        @Bean
        public SimpleMessageListenerContainerFactory messageListenerContainerFactory(
                final AmazonSQSAsync sqs,
                final Logger logger) {
            final var factory = new SimpleMessageListenerContainerFactory();
            final var spy = mock(sqs.getClass(), withSettings()
                    //                    .verboseLogging()
                    .spiedInstance(sqs)
                    .defaultAnswer(CALLS_REAL_METHODS));
            factory.setAmazonSqs(spy);

            doAnswer(invocation -> {
                try {
                    final var result = invocation.callRealMethod();
                    logger.info("!!! AWS SEND worked: {}", result);
                    return result;
                } catch (final AmazonServiceException e) {
                    logger.error("!!! AWS SEND failed: {}",
                            e.toString(), e);
                    throw e;
                }
            }).when(spy).sendMessage(any());

            doAnswer(invocation -> {
                try {
                    final var result = invocation.callRealMethod();
                    logger.info("!!! AWS RECEIVE worked: {}", result);
                    return result;
                } catch (final AmazonServiceException e) {
                    logger.error("!!! AWS RECEIVE failed: {}",
                            e.toString(), e);
                    throw e;
                }
            }).when(spy)
                    .receiveMessage(Mockito.<ReceiveMessageRequest>any());

            return factory;
        }
    }

    @Configuration
    public static class LoggingConfiguration {
        @Bean
        @Scope(SCOPE_PROTOTYPE)
        public Logger logger(final InjectionPoint at) {
            return getLogger(requireNonNull(at.getMethodParameter())
                    .getContainingClass());
        }
    }

    @Component
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public static class Pub
            implements ApplicationListener<ApplicationReadyEvent> {
        private final AmazonSQSAsync sqs;
        private final Logger logger;

        @Override
        public void onApplicationEvent(final ApplicationReadyEvent event) {
            final var queue = sqs.createQueue(queueName);
            logger.info("Purging queue");
            sqs.purgeQueue(new PurgeQueueRequest(queue.getQueueUrl()));
            logger.info("Queue purged");

            final var foo = new Foo();
            foo.number = 3;
            logger.info("Sending a Foo! {}", foo);
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
