package x.txns;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FooListener {
    private final FooRepository foos;
    private final Logger logger;

    @Async
    @TransactionalEventListener
    public void handleFoo(final FooEvent event) {
        logger.info("POST-COMMIT: {}",
                foos.findById(event.getId()).orElseThrow());
    }
}
