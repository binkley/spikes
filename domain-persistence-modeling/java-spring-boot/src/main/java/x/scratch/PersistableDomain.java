package x.scratch;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.annotation.Nonnull;

import static lombok.AccessLevel.PRIVATE;

public interface PersistableDomain<Snapshot,
        Domain extends PersistableDomain<Snapshot, Domain>> {
    @Nonnull
    String getNaturalId();

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

    UpsertedDomainResult<Snapshot, Domain> save();

    void delete();

    Snapshot toSnapshot();

    @RequiredArgsConstructor(access = PRIVATE)
    @Value
    class UpsertedDomainResult<Snapshot,
            Domain extends PersistableDomain<Snapshot, Domain>> {
        private final Domain domain;
        private final boolean changed;

        public static <Snapshot,
                Domain extends PersistableDomain<Snapshot, Domain>>
        UpsertedDomainResult<Snapshot, Domain> of(
                final Domain domain, final boolean changed) {
            return new UpsertedDomainResult<>(domain, changed);
        }
    }
}
