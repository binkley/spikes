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
    FooEvent(final FooRecord foo) {
        super(foo);
    }

    Long getId() {
        return ((FooRecord) getSource()).getId();
    }
}
