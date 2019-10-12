package x.scratch.parent;

import x.scratch.ScopedMutable;
import x.scratch.UpsertableDomain;

public interface Parent extends ParentDetails,
        ScopedMutable<Parent, MutableParent>,
        UpsertableDomain<Parent> {
    ParentResource toResource();
}
