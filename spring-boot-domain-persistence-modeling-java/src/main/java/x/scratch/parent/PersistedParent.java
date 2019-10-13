package x.scratch.parent;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;
import x.scratch.UpsertableRecord.UpsertedRecordResult;
import x.scratch.child.Child;
import x.scratch.child.MutableChild;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

@EqualsAndHashCode(exclude = "factory")
@ToString(exclude = "factory")
final class PersistedParent
        implements Parent {
    private final PersistedParentFactory factory;
    private final Set<Child> children;
    private ParentResource snapshot;
    @Delegate(types = ParentDetails.class)
    private ParentRecord record;
    private Set<Child> snapshotChildren;

    PersistedParent(final PersistedParentFactory factory,
            final ParentResource snapshot,
            final ParentRecord record,
            final Stream<Child> assigned) {
        this.factory = factory;
        this.snapshot = snapshot;
        this.record = record;
        final var chidren = assigned.collect(toCollection(TreeSet::new));
        children = chidren;
        snapshotChildren = new TreeSet<>(chidren);
    }

    @Override
    public boolean isChanged() {
        return !Objects.equals(snapshot, toResource());
    }

    @Override
    public UpsertedDomainResult<Parent> save() {
        // Save ourselves first, so children have a valid parent
        final var before = snapshot;
        final var result = isChanged()
                ? factory.save(record)
                : UpsertedRecordResult.of(record, null);
        record = result.getRecord();

        assignedChildren().forEach(Child::save);
        unassignedChildren().forEach(Child::save);
        changedChildren().forEach(Child::save);
        // Update our version -- TODO: Optimize away if unneeded
        final var refreshed = factory.refresh(getNaturalId());
        record.setVersion(refreshed.getVersion());

        snapshotChildren = new TreeSet<>(children);
        final var after = toResource();
        snapshot = after;
        factory.notifyChanged(before, after);
        return UpsertedDomainResult.of(this, result.isChanged());
    }

    @Override
    public void delete() {
        if (!getChildren().isEmpty())
            throw new IllegalStateException();

        snapshotChildren.forEach(Child::save);

        final var before = snapshot;
        final var after = (ParentResource) null;
        factory.delete(record);
        record = null;
        snapshot = after;
        factory.notifyChanged(before, after);
    }

    private Set<Child> assignedChildren() {
        final var assigned = new TreeSet<>(children);
        assigned.removeAll(snapshotChildren);
        return assigned;
    }

    private Set<Child> unassignedChildren() {
        final var unassigned = new TreeSet<>(snapshotChildren);
        unassigned.removeAll(children);
        return unassigned;
    }

    private Set<Child> changedChildren() {
        final var changed = new TreeSet<>(snapshotChildren);
        changed.retainAll(children);
        return changed.stream()
                .filter(Child::isChanged)
                .collect(toCollection(TreeSet::new));
    }

    @Nonnull
    @Override
    public Set<Child> getChildren() {
        return unmodifiableSet(children);
    }

    @Override
    public ParentResource toResource() {
        return PersistedParentFactory.toResource(record);
    }

    @Override
    public Parent update(final Consumer<MutableParent> block) {
        final var mutable = new PersistedMutableParent(
                record, children, this::addChild, this::removeChild);
        block.accept(mutable);
        return this;
    }

    private void addChild(final Child child, final Set<Child> all) {
        child.update(it -> it.assignTo(this));
        children.add(child);
    }

    private void removeChild(final Child child, final Set<Child> all) {
        child.update(MutableChild::unassignFromAny);
        children.remove(child);
    }
}
