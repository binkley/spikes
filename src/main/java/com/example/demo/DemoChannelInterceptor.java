package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoChannelInterceptor implements ChannelInterceptor {
    private final Logger logger;

    @Override
    public void afterSendCompletion(final Message<?> message,
            final MessageChannel channel,
            final boolean sent, final Exception ex) {
        logger.info("Hi, mom!");
    }

    @Override
    public void afterReceiveCompletion(final Message<?> message,
            final MessageChannel channel, final Exception ex) {
        logger.info("Bye, mom!");
    }
}
