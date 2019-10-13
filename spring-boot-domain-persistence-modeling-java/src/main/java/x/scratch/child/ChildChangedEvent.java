package x.scratch.child;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import x.scratch.DomainChangedEvent;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ChildChangedEvent
        extends DomainChangedEvent<ChildResource> {
    public ChildChangedEvent(final ChildResource before,
            final ChildResource after) {
        super(before, after);
    }
}
