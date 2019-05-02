package com.example.demo;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import io.smartup.localstack.EnableLocalStack;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
import org.springframework.core.style.ToStringCreator;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import static java.lang.String.format;
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
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

@EnableLocalStack
@SpringBootApplication
public class DemoApplication {
    private static final String queueName = "foos";
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(final String... args)
            throws InterruptedException {
        System.setProperty("aws.accessKeyId", "foo");
        System.setProperty("aws.secretKey", "bar");

        try (final var context = SpringApplication.run(
                DemoApplication.class, args)) {
            context.getBean(Pub.class).run();
            latch.await();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T noisySpy(final T realInstance,
            final boolean noisy) {
        var mockSettings = withSettings()
                .spiedInstance(realInstance)
                .defaultAnswer(CALLS_REAL_METHODS);
        if (noisy) mockSettings = mockSettings.verboseLogging();

        return (T) mock(realInstance.getClass(), mockSettings);
    }

    @SuppressWarnings("unchecked")
    private static <T> T spyOn(final Object target,
            final String fieldName, final boolean noisy) {
        final var field = findField(target.getClass(), fieldName);
        if (null == field) throw new IllegalStateException(
                format("No field '%s' found in %s or any superclass",
                        fieldName, target.getClass()));
        makeAccessible(field);

        final T fieldValue = (T) getField(field, target);
        final T spy = noisySpy(fieldValue, noisy);

        setField(field, target, spy);

        return spy;
    }

    @Configuration
    public static class DemoConfiguration {
        @Bean
        public AWSCredentialsProvider awsCredentialsProvider() {
            return new SystemPropertiesCredentialsProvider();
        }

        @Bean
        public SimpleMessageListenerContainerFactory messageListenerContainerFactory(
                final AmazonSQSAsync sqs,
                final LoggingForAmazonHttpClient answer) {
            final AmazonSQSAsyncClient realSQS = spyOn(sqs, "realSQS", false);
            final AmazonHttpClient client = spyOn(realSQS, "client", false);

            doAnswer(answer)
                    .when(client).execute(any(), any(), any(), any());

            final var factory = new SimpleMessageListenerContainerFactory();
            factory.setAmazonSqs(sqs);
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

    @Component
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public static class LoggingForAmazonHttpClient
            implements Answer<Response<?>> {
        private final Logger logger;

        @Override
        public Response<?> answer(final InvocationOnMock invocation)
                throws Throwable {
            final Request<?> request = invocation.getArgument(0);
            try {
                final var response
                        = (Response<?>) invocation.callRealMethod();
                logger.debug("AWS HTTP: {}: {}",
                        request, response.getAwsResponse());
                return response;
            } catch (final AmazonServiceException e) {
                final var eToString = new ToStringCreator(e)
                        .append("errorCode", e.getErrorCode())
                        .append("errorMessage", e.getErrorMessage())
                        .append("errorType", e.getErrorType())
                        .append("statusCode", e.getStatusCode())
                        .append("serviceName", e.getServiceName())
                        .toString();
                logger.error("AWS HTTP service failed: {}: {}",
                        request, eToString, e);
                throw e;
            } catch (final AmazonClientException e) {
                logger.error("AWS HTTP client failed: {}: {}",
                        request, e.toString(), e);
                throw e;
            }
        }
    }
}
