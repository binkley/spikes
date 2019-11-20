package x.scratch.child;

import x.scratch.parent.ParentSimpleDetails;

public interface MutableChild
        extends MutableChildSimpleDetails {
    void assignTo(ParentSimpleDetails parent);

    void unassignFromAny();
}
