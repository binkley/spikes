package x.txns;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WhenReady
        implements ApplicationListener<ApplicationReadyEvent> {
    private final FooRepository foos;
    private final ApplicationEventPublisher publisher;
    private final Logger logger;

    @Override
    @Transactional
    public void onApplicationEvent(final ApplicationReadyEvent ignored) {
        final var saved = foos.save(new Foo(null, "BAR", 3));
        logger.info("SAVED: {}", saved);

        foos.readAll()
                .map(FooEvent::new)
                .peek(publisher::publishEvent)
                .forEach(event -> logger.info("PUBLISHED: {}", event));

        logger.info("END OF PUBLISHING");
    }
}
