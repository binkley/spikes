package x.scratch.child;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import x.scratch.DomainChangedEvent;
import x.scratch.LiveTestBase;
import x.scratch.PersistableDomain.UpsertedDomainResult;
import x.scratch.SqlQueries;
import x.scratch.TestListener;
import x.scratch.parent.ParentFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistedChildTest
        extends LiveTestBase {
    @Autowired
    PersistedChildTest(final ParentFactory parents,
            final ChildFactory children,
            final SqlQueries sqlQueries,
            final TestListener<DomainChangedEvent<?>> testListener) {
        super(parents, children, sqlQueries, testListener);
    }

    @Test
    void shouldCreateNew() {
        final var foundOrCreated = children
                .findExistingOrCreateNewUnassigned(childNaturalId);

        assertSqlQueryTypesByCount(Map.of("SELECT", 1L));
        assertThat(foundOrCreated).isEqualTo(
                children.createNewUnassigned(childNaturalId));
        assertThat(foundOrCreated.isExisting()).isFalse();
    }

    @Test
    void shouldFindExisting() {
        final var saved = newSavedUnassignedChild();

        final var found = children
                .findExistingOrCreateNewUnassigned(childNaturalId);

        assertSqlQueryTypesByCount(Map.of("SELECT", 1L));
        assertThat(found).isEqualTo(saved);
        assertThat(found.isExisting()).isTrue();
    }

    @Test
    void shouldRoundTrip() {
        final var unsaved = children.createNewUnassigned(childNaturalId);

        assertThat(unsaved.getVersion()).isEqualTo(0);
        assertThat(events()).isEmpty();

        final var saved = unsaved.save();

        assertThat(children.all()).hasSize(1);
        assertThat(unsaved.getVersion()).isEqualTo(1);
        assertThat(saved).isEqualTo(UpsertedDomainResult.of(unsaved, true));

        assertDomainChangedEvents(new ChildChangedEvent(
                null,
                childSnapshot().version(1).build()));

        assertThat(currentPersistedChild()).isEqualTo(unsaved);
    }

    @Test
    void shouldDetectNoChanges() {
        final var original = newSavedUnassignedChild();
        final var resaved = original.save();

        assertThat(resaved)
                .isEqualTo(UpsertedDomainResult.of(original, false));
        assertThat(events()).isEmpty();
    }

    @Test
    void shouldMutate() {
        final var original = newSavedUnassignedChild();

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
        assertDomainChangedEvents(new ChildChangedEvent(
                childSnapshot().value(null).version(1).build(),
                childSnapshot().value(value).version(2).build()));
    }

    @Test
    void shouldDelete() {
        final var existing = newSavedUnassignedChild();

        existing.delete();

        assertSqlQueryTypesByCount(Map.of("DELETE", 1L));
        assertAllChildren().isEmpty();
        assertThatThrownBy(existing::getVersion)
                .isInstanceOf(NullPointerException.class);

        assertDomainChangedEvents(new ChildChangedEvent(
                childSnapshot().version(1).build(),
                null));
    }

    @Test
    void shouldAssignChildAtCreation() {
        final var parent = newSavedParent();

        assertThat(parent.getVersion()).isEqualTo(1);

        final var unsaved = children.createNewUnassigned(childNaturalId)
                .update(it -> it.assignTo(parent));

        assertThat(unsaved.getParentNaturalId()).isEqualTo(parentNaturalId);

        unsaved.save();

        assertThat(currentPersistedChild().getParentNaturalId())
                .isEqualTo(parentNaturalId);
        assertThat(currentPersistedParent().getVersion()).isEqualTo(2);
        assertDomainChangedEvents(new ChildChangedEvent(
                null,
                childSnapshot().assigned().version(1).build()));
    }

    @Test
    void shouldAssignChildAtMutation() {
        final var parent = newSavedParent();
        final var child = newSavedUnassignedChild();

        assertThat(parent.getVersion()).isEqualTo(1);

        final var assigned = child.update(it -> it.assignTo(parent));

        assertThat(assigned.getParentNaturalId()).isEqualTo(parentNaturalId);
        assertThat(events()).isEmpty();

        assigned.save();

        assertThat(assigned.getVersion()).isEqualTo(2);
        assertThat(currentPersistedChild().getParentNaturalId())
                .isEqualTo(parentNaturalId);
        assertThat(currentPersistedParent().getVersion()).isEqualTo(2);
        assertDomainChangedEvents(new ChildChangedEvent(
                childSnapshot().unassigned().version(1).build(),
                childSnapshot().assigned().version(2).build()));
    }

    @Test
    void shouldUnassignChild() {
        final var parentChild = newSavedAssignedChild();
        final var parent = parentChild.parent;
        final var child = parentChild.child;

        assertThat(parent.getVersion()).isEqualTo(2);

        child.update(MutableChild::unassignFromAny).save();

        assertThat(child.getVersion()).isEqualTo(2);
        assertThat(currentPersistedChild().getParentNaturalId()).isNull();
        // Created, assigned and unassigned by the child == version 3
        assertThat(currentPersistedParent().getVersion()).isEqualTo(3);
        assertDomainChangedEvents(new ChildChangedEvent(
                childSnapshot().assigned().version(1).build(),
                childSnapshot().unassigned().version(2).build()));
    }
}
