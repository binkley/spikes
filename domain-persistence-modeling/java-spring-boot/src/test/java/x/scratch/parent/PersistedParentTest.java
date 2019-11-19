package x.scratch.parent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import x.scratch.DomainChangedEvent;
import x.scratch.DomainException;
import x.scratch.LiveTestBase;
import x.scratch.PersistableDomain.UpsertedDomainResult;
import x.scratch.SqlQueries;
import x.scratch.TestListener;
import x.scratch.child.ChildChangedEvent;
import x.scratch.child.ChildFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static x.scratch.parent.MutableParent.Helper.assign;
import static x.scratch.parent.MutableParent.Helper.unassign;

class PersistedParentTest
        extends LiveTestBase {
    @Autowired
    PersistedParentTest(final ParentFactory parents,
            final ChildFactory children,
            final SqlQueries sqlQueries,
            final TestListener<DomainChangedEvent<?>> testListener) {
        super(parents, children, sqlQueries, testListener);
    }

    @Test
    void shouldCreateNew() {
        final var foundOrCreated = parents
                .findExistingOrCreateNew(parentNaturalId);

        assertSqlQueryTypesByCount(Map.of("SELECT", 1L));
        assertThat(foundOrCreated).isEqualTo(
                parents.createNew(parentNaturalId));
        assertThat(foundOrCreated.getChildren()).isEmpty();
    }

    @Test
    void shouldFindExisting() {
        final var saved = newSavedParent();

        final var foundOrCreated = parents
                .findExistingOrCreateNew(parentNaturalId);

        assertSqlQueryTypesByCount(Map.of("SELECT", 1L));
        assertThat(foundOrCreated).isEqualTo(saved);
        assertThat(foundOrCreated.getChildren()).isEmpty();
    }

    @Test
    void shouldRoundTrip() {
        final var unsaved = parents.createNew(parentNaturalId);

        assertThat(unsaved.getVersion()).isEqualTo(0);
        assertThat(events()).isEmpty();

        final var saved = unsaved.save();

        assertThat(parents.all()).hasSize(1);
        assertThat(unsaved.getVersion()).isEqualTo(1);
        assertThat(saved).isEqualTo(UpsertedDomainResult.of(unsaved, true));
        assertDomainChangedEvents(new ParentChangedEvent(
                null,
                parentSnapshot().version(1).build()));

        assertThat(currentPersistedParent()).isEqualTo(unsaved);
    }

    @Test
    void shouldDetectNoChanges() {
        final var original = newSavedParent();
        final var resaved = original.save();

        assertThat(resaved).isEqualTo(
                UpsertedDomainResult.of(original, false));
        assertThat(events()).isEmpty();
    }

    @Test
    void shouldMutate() {
        final var original = newSavedParent();

        assertThat(original.isChanged()).isFalse();

        final var value = "FOOBAR";
        final var modified = original.update(it -> it.setValue(value));

        assertThat(modified).isEqualTo(original);
        assertThat(original.isChanged()).isTrue();
        assertThat(original.getValue()).isEqualTo(value);
        assertThat(events()).isEmpty();

        original.save();

        assertSqlQueryTypesByCount(Map.of("UPSERT", 1L));
        assertThat(original.isChanged()).isFalse();
        assertDomainChangedEvents(new ParentChangedEvent(
                parentSnapshot().value(null).version(1).build(),
                parentSnapshot().value(value).version(2).build()));
    }

    @Test
    void shouldMutateChildren() {
        final var parentChild = newSavedAssignedChild();
        final var parent = parentChild.parent;

        final var value = "FOOBAR";
        // Silly example :)
        parent.update(it ->
                it.getChildren().forEach(itt ->
                        itt.update(ittt ->
                                ittt.setValue(value))));
        parent.save();

        assertThat(currentPersistedChild().getValue()).isEqualTo(value);

        assertDomainChangedEvents(
                new ChildChangedEvent(
                        childSnapshot().assigned().value(null).version(1)
                                .build(),
                        childSnapshot().assigned().value(value).version(2)
                                .build()),
                new ParentChangedEvent(
                        parentSnapshot().version(2).build(),
                        parentSnapshot().version(3).build()));
    }

    @Test
    void shouldDelete() {
        final var existing = newSavedParent();

        existing.delete();

        assertSqlQueryTypesByCount(Map.of("DELETE", 1L));
        assertAllParents().isEmpty();
        assertThatThrownBy(existing::getVersion)
                .isInstanceOf(NullPointerException.class);
        assertEvents(new ParentChangedEvent(
                parentSnapshot().version(1).build(),
                null));
    }

    @Test
    void shouldNotDelete() {
        final var parent = newSavedParent();
        final var child = newSavedUnassignedChild();

        parent.update(assign(child));

        assertThatThrownBy(parent::delete)
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotAssignAlreadyAssignedChild() {
        final var parentChild = newSavedAssignedChild();
        final var parent = parentChild.parent;
        final var child = parentChild.child;

        assertThatThrownBy(() -> parent.update(assign(child)).save())
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldAssignAndUnassignChild() {
        final var parent = newSavedParent();
        final var child = newSavedUnassignedChild();

        assertThat(parent.getChildren()).isEmpty();

        final var childAssigned = parent
                .update(assign(child))
                .save().getDomain();

        assertThat(parent.getChildren()).containsExactly(child);
        assertThat(childAssigned.getVersion()).isEqualTo(2);
        assertThat(currentPersistedChild().getParentNaturalId())
                .isEqualTo(parentNaturalId);
        assertDomainChangedEvents(
                new ChildChangedEvent(
                        childSnapshot().unassigned().version(1).build(),
                        childSnapshot().assigned().version(2).build()),
                new ParentChangedEvent(
                        parentSnapshot().version(1).build(),
                        parentSnapshot().version(2).build()));

        final var childUnassigned = parent
                .update(unassign(child))
                .save().getDomain();

        assertThat(parent.getChildren()).isEmpty();
        assertThat(childUnassigned.getVersion()).isEqualTo(3);
        assertThat(currentPersistedChild().getParentNaturalId()).isNull();
        assertDomainChangedEvents(
                new ChildChangedEvent(
                        childSnapshot().assigned().version(2).build(),
                        childSnapshot().unassigned().version(3).build()),
                new ParentChangedEvent(
                        parentSnapshot().version(2).build(),
                        parentSnapshot().version(3).build()));
    }
}
