package x.txns;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FooListener {
    private final Logger logger;

    @EventListener
    public void handleFoo(final FooEvent event) {
        logger.info("PUBLISHED: {}", event.getSource());
    }
}
