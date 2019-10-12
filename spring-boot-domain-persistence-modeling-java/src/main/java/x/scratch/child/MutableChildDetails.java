package x.scratch.child;

public interface MutableChildDetails extends ChildDetails {
    void setParentNaturalId(String parentNaturalId);

    void setValue(String newValue);
}
