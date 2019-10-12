package x.scratch.child;

import x.scratch.ScopedMutable;
import x.scratch.UpsertableDomain;
import x.scratch.child.MutableChild;

public interface Child extends ChildDetails,
        ScopedMutable<Child, MutableChild>,
        UpsertableDomain<Child> {
    ChildResource toResource();
}
