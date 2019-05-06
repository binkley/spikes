package x.txns;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString(callSuper = true)
public class FooEvent
        extends ApplicationEvent {
    public FooEvent(final Foo foo) {
        super(foo);
    }

    public Long getId() {
        return ((Foo) getSource()).getId();
    }
}
