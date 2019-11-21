package x.scratch.child;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;
import x.scratch.DomainException;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static lombok.AccessLevel.PACKAGE;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(exclude = "factory")
@ToString(exclude = "factory")
final class PersistedChild
        implements Child {
    private final PersistedChildFactory factory;
    private ChildSnapshot snapshot;
    @Delegate(types = JavaWorkaroundChildSimpleDetails.class)
    private ChildRecord record;

    @Nonnull
    @Override
    public Set<String> getDefaultSideValues() {
        return unmodifiableSet(record.getDefaultSideValues());
    }

    @Nonnull
    @Override
    public Set<String> getSideValues() {
        return unmodifiableSet(record.getSideValues());
    }

    @Override
    public boolean isChanged() {
        return !Objects.equals(snapshot, toSnapshot());
    }

    @Override
    public UpsertedDomainResult<ChildSnapshot, Child> save() {
        if (!isChanged()) return UpsertedDomainResult.of(this, false);

        final var before = snapshot;
        final var result = factory.save(record);
        record = result.getRecord();
        final var after = toSnapshot();
        snapshot = after;
        if (result.isChanged()) // Trust the database
            factory.notifyChanged(before, after);
        return UpsertedDomainResult.of(this, result.isChanged());
    }

    @Override
    public void delete() {
        if (null != getParentNaturalId())
            throw new DomainException(format(
                    "Deleting child assigned to a parent: %s", this));

        final var before = snapshot;
        final var after = (ChildSnapshot) null;
        factory.delete(record);
        record = null;
        snapshot = after;
        factory.notifyChanged(before, after);
    }

    @Override
    public ChildSnapshot toSnapshot() {
        return PersistedChildFactory.toSnapshot(record);
    }

    @Override
    public <R> R updateTo(final Function<MutableChild, R> block) {
        return block.apply(new PersistedMutableChild(record));
    }

    @Override
    public Child update(final Consumer<MutableChild> block) {
        block.accept(new PersistedMutableChild(record));
        return this;
    }

    private interface JavaWorkaroundChildSimpleDetails {
        @Nonnull
        String getNaturalId();

        String getParentNaturalId();

        String getValue();

        int getVersion();
    }
}
