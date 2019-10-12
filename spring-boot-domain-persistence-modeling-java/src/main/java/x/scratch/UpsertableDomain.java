package x.scratch;

import lombok.Value;

public interface UpsertableDomain<Domain extends UpsertableDomain<Domain>> {
    boolean isExisting();

    int getVersion();

    UpsertedDomainResult<Domain> save();

    void delete();

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
