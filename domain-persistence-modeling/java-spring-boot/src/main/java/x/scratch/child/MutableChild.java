package x.scratch.child;

import x.scratch.parent.ParentDetails;

public interface MutableChild
        extends MutableChildDetails {
    void assignTo(ParentDetails parent);

    void unassignFromAny();
}
