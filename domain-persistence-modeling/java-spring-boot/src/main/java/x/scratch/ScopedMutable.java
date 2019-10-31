package x.scratch;

import java.util.function.Consumer;

public interface ScopedMutable<Domain, Mutable> {
    Domain update(Consumer<Mutable> block);
}
