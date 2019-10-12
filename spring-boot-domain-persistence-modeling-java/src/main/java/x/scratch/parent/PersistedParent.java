package x.scratch.parent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

import java.util.function.Consumer;

@AllArgsConstructor
@EqualsAndHashCode(exclude = "factory")
@ToString(exclude = "factory")
public final class PersistedParent implements Parent {
    private final PersistedParentFactory factory;
    private ParentResource snapshot;
    @Delegate(types = ParentDetails.class)
    private ParentRecord record;

    @Override
    public boolean isExisting() {
        return 0 < getVersion();
    }

    @Override
    public UpsertedDomainResult<Parent> save() {
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
        final var after = (ParentResource) null;
        factory.delete(record);
        record = null;
        snapshot = after;
        factory.notifyChanged(before, after);
    }

    @Override
    public Parent update(final Consumer<MutableParent> block) {
        final var mutable = new PersistedMutableParent(record);
        block.accept(mutable);
        return this;
    }

    @Override
    public ParentResource toResource() {
        return PersistedParentFactory.toResource(record);
    }
}
