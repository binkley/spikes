package x.scratch.parent;

import x.scratch.ScopedMutable;
import x.scratch.UpsertableDomain;
import x.scratch.child.Child;

import javax.annotation.Nonnull;
import java.util.Set;

public interface Parent
        extends ParentDetails,
        ScopedMutable<Parent, MutableParent>,
        UpsertableDomain<Parent> {
    @Nonnull
    Set<Child> getChildren(); // Immutable

    ParentResource toResource();
}
