package x.txns;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;
import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RunIt
        implements ApplicationListener<ApplicationReadyEvent> {
    private final FooRepository foos;
    private final FailedNestedTransaction nested;
    private final ApplicationEventPublisher publisher;
    private final Logger logger;

    @Override
    @Transactional
    public void onApplicationEvent(final ApplicationReadyEvent ready) {
        final var saved = foos.save(new FooRecord(
                null, randomUUID().toString(), 3));
        logger.info("SAVED: {}", saved);

        try {
            nested.failedSave(saved);
        } catch (final DbActionExecutionException e) {
            logger.warn("FAILED NESTED TRANSACTION: {}",
                    getMostSpecificCause(e).getMessage());
        }

        logger.warn("BRAVELY CONTINUED ON");

        foos.readAll()
                .map(FooEvent::new)
                .peek(publisher::publishEvent)
                .forEach(event -> logger.info("PUBLISHED: {}", event));

        logger.info("END OF PUBLISHING");
    }
}
