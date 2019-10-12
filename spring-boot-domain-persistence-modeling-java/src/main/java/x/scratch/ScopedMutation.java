package x.scratch;

import java.util.function.Consumer;

public interface ScopedMutation<Domain, Mutable> {
    Domain update(Consumer<Mutable> block);

    Domain save();

    void delete();
}
