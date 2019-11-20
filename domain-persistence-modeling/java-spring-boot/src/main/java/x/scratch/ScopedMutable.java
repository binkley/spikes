package x.scratch;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ScopedMutable<Domain, Mutable> {
    /**
     * Updates the mutable, and returns update result from the
     * <var>block</var>. This is a workaround for Java language limitations.
     */
    <R> R updateTo(Function<Mutable, R> block);

    /**
     * Updates the mutable, and returns the domain object from the
     * <var>block</var>. This is a workaround for Java language limitations.
     */
    Domain update(Consumer<Mutable> block);
}
