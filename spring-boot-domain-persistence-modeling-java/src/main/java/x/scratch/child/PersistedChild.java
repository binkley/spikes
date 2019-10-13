package x.scratch.child;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableSet;
import static lombok.AccessLevel.PACKAGE;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(exclude = "factory")
@ToString(exclude = "factory")
public final class PersistedChild
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
    public UpsertedDomainResult<Child> save() {
        if (!isChanged()) return UpsertedDomainResult.of(this, false);

        final var before = snapshot;
        final var result = factory.save(record);
        record = result.getRecord();
        final var after = toResource();
        snapshot = after;
        factory.notifyChanged(before, after);
        return UpsertedDomainResult.of(this, result.isChanged());
    }

    @Override
    public void delete() {
        final var before = snapshot;
        final var after = (ChildResource) null;
        factory.delete(record);
        record = null;
        snapshot = after;
        factory.notifyChanged(before, after);
    }

    @Override
    public Child update(final Consumer<MutableChild> block) {
        final var mutable = new PersistedMutableChild(record);
        block.accept(mutable);
        return this;
    }

    @Override
    public ChildResource toResource() {
        return PersistedChildFactory.toResource(record);
    }
}
