package x.txns;

import org.springframework.context.ApplicationEvent;

public class FooEvent
        extends ApplicationEvent {
    public FooEvent(final Object source) {
        super(source);
    }
}
