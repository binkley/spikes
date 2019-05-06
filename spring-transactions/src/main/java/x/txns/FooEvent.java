package x.txns;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString(callSuper = true)
class FooEvent
        extends ApplicationEvent {
    FooEvent(final Foo foo) {
        super(foo);
    }

    Long getId() {
        return ((Foo) getSource()).getId();
    }
}
