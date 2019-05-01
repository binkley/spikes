package com.example.demo;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.cloud.aws.messaging.core.QueueMessageChannel;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;

public class DemoQueueMessagingTemplate extends QueueMessagingTemplate {
    private final DemoChannelInterceptor interceptor;

    public DemoQueueMessagingTemplate(final AmazonSQSAsync sqs,
            final DemoChannelInterceptor interceptor) {
        super(sqs);
        this.interceptor = interceptor;
    }

    @Override
    protected QueueMessageChannel resolveMessageChannel(
            final String physicalResourceIdentifier) {
        final var channel = super.resolveMessageChannel(physicalResourceIdentifier);
        channel.addInterceptor(interceptor);
        return channel;
    }
}
