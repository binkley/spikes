package x.scratch.child;

import java.util.Optional;
import java.util.stream.Stream;

public interface ChildFactory {
    Stream<Child> all();

    Optional<Child> findExisting(String naturalId);

    Child createNew(String naturalId);

    Child findExistingOrCreateNew(String naturalId);

    Stream<Child> findOwned(String parentNaturalId);
}
