package com.example.demo;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.example.demo.Spies.spyOn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@Configuration
public class SqsConfiguration {
    @Bean
    @Primary
    public AWSCredentialsProvider localStackAwsCredentials() {
        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials("foo", "bar"));
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
