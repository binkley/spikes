package x.scratch.parent;

import x.scratch.ScopedMutation;
import x.scratch.UpsertableDomain;

public interface Parent extends ParentDetails,
        ScopedMutation<Parent, MutableParent>,
        UpsertableDomain<Parent> {
    ParentResource toResource();
}
