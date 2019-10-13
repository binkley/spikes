package x.scratch;

import javax.annotation.Nonnull;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

public class TrackedSortedSet<T extends Comparable<T>>
        extends AbstractSet<T> {
    private final BiConsumer<T, Set<T>> added;
    private final BiConsumer<T, Set<T>> removed;
    private final Set<T> sorted;

    public TrackedSortedSet(final Set<T> initial,
            final BiConsumer<T, Set<T>> added,
            final BiConsumer<T, Set<T>> removed) {
        this.added = added;
        this.removed = removed;
        sorted = new TreeSet<>(initial);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> it = sorted.iterator();
            private T curr = null;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                curr = it.next();
                return curr;
            }

            @Override
            public void remove() {
                it.remove();
                removed.accept(curr, sorted);
            }
        };
    }

    @Override
    public int size() {
        return sorted.size();
    }

    @Override
    public boolean add(final T t) {
        final var add = sorted.add(t);
        if (add) added.accept(t, sorted);
        return add;
    }
}
