package x.scratch.child;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import x.scratch.DomainChangedEvent;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ChildChangedEvent
        extends DomainChangedEvent<ChildSnapshot> {
    public ChildChangedEvent(final ChildSnapshot before,
            final ChildSnapshot after) {
        super(before, after);
    }
}
