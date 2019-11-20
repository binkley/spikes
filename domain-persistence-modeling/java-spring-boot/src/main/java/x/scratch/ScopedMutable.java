package x.scratch;

import java.util.function.Consumer;

public interface ScopedMutable<Domain, Mutable> {
    <R> R update(Consumer<Mutable> block);
}
