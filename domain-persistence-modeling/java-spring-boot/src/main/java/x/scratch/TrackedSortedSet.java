package x.scratch;

import javax.annotation.Nonnull;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

public class TrackedSortedSet<T extends Comparable<? super T>>
        extends AbstractSet<T> {
    private final Set<T> sorted;
    private final BiConsumer<T, Set<T>> added;
    private final BiConsumer<T, Set<T>> removed;

    public TrackedSortedSet(final Set<T> initial,
            final BiConsumer<T, Set<T>> added,
            final BiConsumer<T, Set<T>> removed) {
        sorted = new TreeSet<>(initial);
        this.added = added;
        this.removed = removed;
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return sorted.iterator();
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

    /**
     * Note the business with casting to <code>T</code>.  This is because Java
     * retrofitted generics onto non-generic classes, and so "remove" must
     * accept non-T arguments for backwards compatibility.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(final Object o) {
        final var t = (T) o;
        final var remove = sorted.remove(t);
        if (remove) removed.accept(t, sorted);
        return remove;
    }
}
