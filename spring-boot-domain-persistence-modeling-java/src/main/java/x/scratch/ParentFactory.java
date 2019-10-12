package x.scratch;

import java.util.Optional;
import java.util.stream.Stream;

public interface ParentFactory {
    Stream<Parent> all();

    Optional<Parent> findExisting(String naturalId);

    Parent createNew(String naturalId);

    Parent findExistingOrCreateNew(String naturalId);
}
