package x.txns;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FooConcurrentListener {
    private final FooRepository foos;
    private final Logger logger;

    @TransactionalEventListener
    public void handleFoo(final FooEvent event) {
        logger.info("POST-COMMIT: {}", foos.findById(event.getId()));
    }
}
