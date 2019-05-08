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
    private final ANestedTransaction nested;
    private final ApplicationEventPublisher publisher;
    private final Logger logger;

    @Override
    @Transactional
    public void onApplicationEvent(final ApplicationReadyEvent ready) {
        final var saved = foos.save(new FooRecord(null, "BAR", 3));
        logger.info("SAVED: {}", saved);

        try {
            nested.undoWrongSave();
        } catch (final NullPointerException ignored) {}

        foos.readAll()
                .map(FooEvent::new)
                .peek(publisher::publishEvent)
                .forEach(event -> logger.info("PUBLISHED: {}", event));

        logger.info("END OF PUBLISHING");
    }
}
