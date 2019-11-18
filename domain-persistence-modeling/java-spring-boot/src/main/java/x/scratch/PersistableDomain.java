package x.scratch;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

public interface PersistableDomain<Resource,
        Domain extends PersistableDomain<Resource, Domain>> {
    int getVersion();

    default boolean isExisting() {
        return 0 < getVersion();
    }

    /**
     * Optimizes to lessen the count of persistence calls.  The code should be
     * correct with or without this optimization: for example, a domain object
     * version number should have the same values.
     */
    boolean isChanged();

    UpsertedDomainResult<Resource, Domain> save();

    void delete();

    Resource toResource();

    @RequiredArgsConstructor(access = PRIVATE)
    @Value
    class UpsertedDomainResult<Resource,
            Domain extends PersistableDomain<Resource, Domain>> {
        private final Domain domain;
        private final boolean changed;

        public static <Resource,
                Domain extends PersistableDomain<Resource, Domain>>
        UpsertedDomainResult<Resource, Domain> of(
                final Domain domain, final boolean changed) {
            return new UpsertedDomainResult<>(domain, changed);
        }
    }
}
