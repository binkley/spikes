package x.scratch.parent;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import x.scratch.DomainChangedEvent;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ParentChangedEvent extends DomainChangedEvent<ParentResource> {
    public ParentChangedEvent(final ParentResource before, final ParentResource after) {
        super(before, after);
    }
}
