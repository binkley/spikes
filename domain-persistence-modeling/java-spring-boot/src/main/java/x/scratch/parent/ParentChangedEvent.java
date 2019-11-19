package x.scratch.parent;

import x.scratch.DomainChangedEvent;

public final class ParentChangedEvent
        extends DomainChangedEvent<ParentSnapshot> {
    public ParentChangedEvent(final ParentSnapshot before,
            final ParentSnapshot after) {
        super(before, after);
    }
}
