package x.scratch.child;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;
import x.scratch.child.PersistedMutableChild;

import java.util.function.Consumer;

@AllArgsConstructor
@EqualsAndHashCode(exclude = "factory")
@ToString(exclude = "factory")
public final class PersistedChild implements Child {
    private final PersistedChildFactory factory;
    private ChildResource snapshot;
    @Delegate(types = ChildDetails.class)
    private ChildRecord record;

    @Override
    public boolean isExisting() {
        return 0 < getVersion();
    }

    @Override
    public UpsertedDomainResult<Child> save() {
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
