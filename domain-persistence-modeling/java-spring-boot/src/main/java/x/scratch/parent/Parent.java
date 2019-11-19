package x.scratch.parent;

import x.scratch.PersistableDomain;
import x.scratch.ScopedMutable;
import x.scratch.child.Child;

import javax.annotation.Nonnull;
import java.util.Set;

public interface Parent
        extends ParentDetails,
        ScopedMutable<Parent, MutableParent>,
        PersistableDomain<ParentSnapshot, Parent> {
    @Nonnull
    Set<Child> getChildren(); // Immutable
}
