package x.scratch.child;

import x.scratch.PersistableDomain;
import x.scratch.ScopedMutable;

public interface Child
        extends ChildSimpleDetails,
        ScopedMutable<Child, MutableChild>,
        PersistableDomain<ChildSnapshot, Child> {
}
