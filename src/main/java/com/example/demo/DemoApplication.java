package com.example.demo;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import io.smartup.localstack.EnableLocalStack;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.cloud.aws.messaging.listener.SendToHandlerMethodReturnValueHandler;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.CountDownLatch;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@EnableLocalStack
@SpringBootApplication
public class DemoApplication {
    private static final String queueName = "foos";
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(final String... args) throws InterruptedException {
        try (final var context = SpringApplication.run(DemoApplication.class, args)) {
            latch.await();
        }
    }

    @Configuration
    public static class DemoConfiguration {
        @Bean
        public SimpleMessageListenerContainerFactory xxx(
                final AmazonSQSAsync sqs) {
            final var factory = new SimpleMessageListenerContainerFactory();
            final var spy = mock(sqs.getClass(), withSettings()
                    .verboseLogging()
                    .spiedInstance(sqs)
                    .defaultAnswer(CALLS_REAL_METHODS));
            factory.setAmazonSqs(sqs);

            return factory;
        }

        @Bean
        public QueueMessageHandlerFactory yyy(final AmazonSQSAsync sqs,
                final DemoChannelInterceptor interceptor) {
            final var template = new DemoQueueMessagingTemplate(sqs, interceptor);

            final var factory = new QueueMessageHandlerFactory() {
                private MappingJackson2MessageConverter getDefaultMappingJackson2MessageConverter() {
                    MappingJackson2MessageConverter jacksonMessageConverter
                            = new MappingJackson2MessageConverter();
                    jacksonMessageConverter.setSerializedPayloadClass(String.class);
                    jacksonMessageConverter.setStrictContentTypeMatch(true);
                    return jacksonMessageConverter;
                }

                @Override
                public QueueMessageHandler createQueueMessageHandler() {
                    final var queueMessageHandler = new DemoQueueMessageHandler(
                            CollectionUtils.isEmpty(getMessageConverters())
                                    ? singletonList(getDefaultMappingJackson2MessageConverter())
                                    : getMessageConverters());

//                    if (!CollectionUtils.isEmpty(this.argumentResolvers)) {
//                        queueMessageHandler.getCustomArgumentResolvers()
//                                .addAll(this.argumentResolvers);
//                    }
//                    if (!CollectionUtils.isEmpty(this.returnValueHandlers)) {
//                        queueMessageHandler.getCustomReturnValueHandlers()
//                                .addAll(this.returnValueHandlers);
//                    }

                    SendToHandlerMethodReturnValueHandler sendToHandlerMethodReturnValueHandler;
                        sendToHandlerMethodReturnValueHandler =
                                new SendToHandlerMethodReturnValueHandler(template);

//                    sendToHandlerMethodReturnValueHandler.setBeanFactory(this.beanFactory);
                    queueMessageHandler.getCustomReturnValueHandlers()
                            .add(sendToHandlerMethodReturnValueHandler);

                    return queueMessageHandler;
                }
            };
            factory.setSendToMessagingTemplate(template);
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
    public static class Pub implements ApplicationListener<ApplicationReadyEvent> {
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
