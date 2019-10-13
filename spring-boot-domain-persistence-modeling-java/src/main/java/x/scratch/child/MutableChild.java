package x.scratch.child;

import x.scratch.parent.Parent;

public interface MutableChild
        extends MutableChildDetails {
    void assignTo(Parent parent);

    void unassignFromAny();
}
