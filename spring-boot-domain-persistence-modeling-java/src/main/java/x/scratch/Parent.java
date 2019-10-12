package x.scratch;

public interface Parent extends ParentDetails,
        ScopedMutation<Parent, MutableParent>,
        UpsertableDomain<Parent> {
    ParentResource toResource();
}
