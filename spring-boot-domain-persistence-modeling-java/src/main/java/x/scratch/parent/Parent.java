package x.scratch.parent;

import x.scratch.ScopedMutable;
import x.scratch.UpsertableDomain;
import x.scratch.child.Child;

public interface Parent
        extends ParentDetails,
        ScopedMutable<Parent, MutableParent>,
        UpsertableDomain<Parent> {
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
