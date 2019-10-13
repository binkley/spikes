package x.scratch.parent;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Delegate;
import x.scratch.TrackedSortedSet;
import x.scratch.child.Child;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.BiConsumer;

@EqualsAndHashCode
@ToString
final class PersistedMutableParent
        implements MutableParent {
    @Delegate(types = MutableParentDetails.class)
    private final @NonNull ParentRecord record;
    private final TrackedSortedSet<Child> children;

    PersistedMutableParent(final ParentRecord record,
            final Set<Child> initial,
            final BiConsumer<Child, Set<Child>> added,
            final BiConsumer<Child, Set<Child>> removed) {
        this.record = record;
        children = new TrackedSortedSet<>(initial, added, removed);
    }

    @Nonnull
    @Override
    public Set<Child> getChildren() {
        return children;
    }
}
