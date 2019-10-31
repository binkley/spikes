package x.scratch.parent;

import x.scratch.DomainChangedEvent;

public final class ParentChangedEvent
        extends DomainChangedEvent<ParentResource> {
    public ParentChangedEvent(final ParentResource before,
            final ParentResource after) {
        super(before, after);
    }
}
