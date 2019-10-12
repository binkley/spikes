package x.scratch;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;
import java.util.function.Consumer;

@AllArgsConstructor
@EqualsAndHashCode(exclude = "factory")
@ToString(exclude = "factory")
public final class PersistedParent implements Parent {
    private final PersistedParentFactory factory;
    private ParentResource snapshot;
    private ParentRecord record;

    @Override
    public String getNaturalId() {
        return Optional.of(record).map(ParentRecord::getNaturalId).orElseThrow();
    }

    @Override
    public String getValue() {
        return Optional.of(record).map(ParentRecord::getValue).orElseThrow();
    }

    @Override
    public int getVersion() {
        return Optional.of(record).map(ParentRecord::getVersion).orElseThrow();
    }

    @Override
    public boolean isExisting() {
        return 0 < getVersion();
    }

    @Override
    public Parent update(final Consumer<MutableParent> block) {
        final var mutable = Optional.of(record).map(PersistedMutableParent::new).orElseThrow();
        block.accept(mutable);
        return this;
    }

    @Override
    public UpsertedDomainResult<Parent> save() {
        final var before = snapshot;
        final var result = Optional.of(record).map(factory::save).orElseThrow();
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
        factory.delete(Optional.of(record).orElseThrow());
        record = null;
        snapshot = after;
        factory.notifyChanged(before, after);
    }

    @Override
    public ParentResource toResource() {
        return Optional.of(record).map(PersistedParentFactory::toResource).orElseThrow();
    }
}
