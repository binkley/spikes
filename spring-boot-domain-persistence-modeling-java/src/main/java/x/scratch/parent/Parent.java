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

    /**
     * @todo This moves to mutable child when parent tracks children, and can
     * save child and update parent record in parent's save
     */
    void assign(Child child);

    /**
     * @todo This moves to mutable child when parent tracks children, and can
     * save child and update parent record in parent's save
     */
    void unassign(Child child);

    ParentResource toResource();
}
