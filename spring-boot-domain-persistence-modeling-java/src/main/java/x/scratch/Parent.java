package x.scratch;

public interface Parent extends ParentDetails,
        ScopedMutation<Parent, MutableParent>,
        Persisted {
    ParentResource toResource();
}
