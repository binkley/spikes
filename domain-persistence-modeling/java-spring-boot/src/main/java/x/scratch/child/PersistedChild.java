package x.scratch.child;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import x.scratch.DomainException;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static lombok.AccessLevel.PACKAGE;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(exclude = "factory")
@ToString(exclude = "factory")
final class PersistedChild
        implements Child {
    private final PersistedChildFactory factory;
    private ChildResource snapshot;
    private ChildRecord record;

    @Nonnull
    @Override
    public String getNaturalId() {
        return record.getNaturalId();
    }

    @Override
    public String getParentNaturalId() {
        return record.getParentNaturalId();
    }

    @Override
    public String getValue() {
        return record.getValue();
    }

    @Nonnull
    @Override
    public Set<String> getSubchildren() {
        return unmodifiableSet(record.getSubchildren());
    }

    @Override
    public int getVersion() {
        return record.getVersion();
    }

    @Override
    public boolean isChanged() {
        return !Objects.equals(snapshot, toResource());
    }

    @Override
    public UpsertedDomainResult<ChildResource, Child> save() {
        if (!isChanged()) return UpsertedDomainResult.of(this, false);

        final var before = snapshot;
        final var result = factory.save(record);
        record = result.getRecord();
        final var after = toResource();
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
        final var after = (ChildResource) null;
        factory.delete(record);
        record = null;
        snapshot = after;
        factory.notifyChanged(before, after);
    }

    @Override
    public ChildResource toResource() {
        return PersistedChildFactory.toResource(record);
    }

    @Override
    public Child update(final Consumer<MutableChild> block) {
        final var mutable = new PersistedMutableChild(record);
        block.accept(mutable);
        return this;
    }
}
