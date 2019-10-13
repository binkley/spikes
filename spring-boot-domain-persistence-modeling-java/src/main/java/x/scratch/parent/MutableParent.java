package x.scratch.parent;

import x.scratch.child.Child;

import javax.annotation.Nonnull;
import java.util.Set;

public interface MutableParent
        extends MutableParentDetails {
    @Nonnull
    Set<Child> getChildren(); // Mutable
}
