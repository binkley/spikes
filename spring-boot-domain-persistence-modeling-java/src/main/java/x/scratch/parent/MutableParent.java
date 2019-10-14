package x.scratch.parent;

import x.scratch.DomainException;
import x.scratch.child.Child;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;

import static java.lang.String.format;

public interface MutableParent
        extends MutableParentDetails {
    @Nonnull
    Set<Child> getChildren(); // Mutable

    default void assign(final Child child) {
        if (!getChildren().add(child))
            throw new DomainException(format("Already assigned: %s", child));
    }

    default void unassign(final Child child) {
        if (!getChildren().remove(child))
            throw new DomainException(format("Not assigned: %s", child));
    }

    final class Helper {
        static Consumer<MutableParent> assign(final Child child) {
            return it -> it.assign(child);
        }

        static Consumer<MutableParent> unassign(final Child child) {
            return it -> it.unassign(child);
        }
    }
}
