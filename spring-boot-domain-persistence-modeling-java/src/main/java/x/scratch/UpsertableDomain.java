package x.scratch;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

public interface UpsertableDomain<Domain extends UpsertableDomain<Domain>> {
    int getVersion();

    default boolean isExisting() {
        return 0 < getVersion();
    }

    /**
     * Optimizes to lessen the number of persistence calls.  The code should
     * be correct with or without this optimization: for example, a domain
     * object version number should have the same values.
     */
    boolean isChanged();

    UpsertedDomainResult<Domain> save();

    void delete();

    @RequiredArgsConstructor(access = PRIVATE)
    @Value
    class UpsertedDomainResult<Domain extends UpsertableDomain<Domain>> {
        private final Domain domain;
        private final boolean changed;

        public static <Domain extends UpsertableDomain<Domain>> UpsertedDomainResult<Domain> of(
                final Domain domain, final boolean changed) {
            return new UpsertedDomainResult<>(domain, changed);
        }
    }
}
