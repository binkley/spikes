package x.scratch.parent;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import x.scratch.DomainChangedEvent;
import x.scratch.DomainException;
import x.scratch.PersistableDomain.UpsertedDomainResult;
import x.scratch.TestListener;
import x.scratch.child.Child;
import x.scratch.child.ChildChangedEvent;
import x.scratch.child.ChildFactory;
import x.scratch.child.ChildResource;

import java.util.List;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static x.scratch.parent.MutableParent.Helper.assign;
import static x.scratch.parent.MutableParent.Helper.unassign;

@AutoConfigureTestDatabase(replace = NONE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest
@TestInstance(PER_CLASS)
@Transactional
class PersistedParentTest {
    private static final String parentNaturalId = "a";
    private static final String childNaturalId = "p";

    private final ParentFactory parents;
    private final ChildFactory children;
    private final TestListener<DomainChangedEvent<?>> testListener;

    @Test
    void shouldCreateNew() {
        final var found = parents.findExistingOrCreateNew(parentNaturalId);

        assertThat(found).isEqualTo(parents.createNew(parentNaturalId));
        assertThat(found.getChildren()).isEmpty();
    }

    @Test
    void shouldFindExisting() {
        final var saved = newSavedParent();

        final var found = parents.findExistingOrCreateNew(parentNaturalId);

        assertThat(found).isEqualTo(saved);
        assertThat(found.getChildren()).isEmpty();
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
        assertThat(events()).containsExactly(new ParentChangedEvent(
                null,
                new ParentResource(parentNaturalId, null, 1)));

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

        assertThat(original.isChanged()).isFalse();
        assertThat(events()).containsExactly(new ParentChangedEvent(
                new ParentResource(parentNaturalId, null, 1),
                new ParentResource(parentNaturalId, value, 2)));
    }

    @Test
    void shouldMutateChildren() {
        final var parent = newSavedParent();
        final var child = newSavedChild();

        parent.update(assign(child)).save();
        testListener.reset();

        final var value = "FOOBAR";
        // Silly example :)
        parent.update(it ->
                it.getChildren().forEach(itt ->
                        itt.update(ittt ->
                                ittt.setValue(value))));
        parent.save();

        assertThat(currentPersistedChild().getValue()).isEqualTo(value);

        assertThat(events()).containsExactly(
                new ChildChangedEvent(
                        new ChildResource(childNaturalId, parentNaturalId,
                                null, emptySet(), 2),
                        new ChildResource(childNaturalId, parentNaturalId,
                                value, emptySet(), 3)),
                new ParentChangedEvent(
                        new ParentResource(parentNaturalId, null, 2),
                        new ParentResource(parentNaturalId, null, 3)));
    }

    @Test
    void shouldDelete() {
        final var existing = newSavedParent();

        existing.delete();

        assertThat(parents.all()).isEmpty();
        assertThatThrownBy(existing::getVersion)
                .isInstanceOf(NullPointerException.class);
        assertThat(events()).containsExactly(new ParentChangedEvent(
                new ParentResource(parentNaturalId, null, 1),
                null));
    }

    @Test
    void shouldNotDelete() {
        final var parent = newSavedParent();
        final var child = newSavedChild();

        parent.update(assign(child));

        assertThatThrownBy(parent::delete)
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotAssignAlreadyAssignedChild() {
        final var parent = newSavedParent();
        final var child = newSavedChild();

        parent.update(assign(child)).save();

        assertThatThrownBy(() -> parent.update(assign(child)).save())
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldAssignAndUnassignChild() {
        final var parent = newSavedParent();
        final var child = newSavedChild();

        assertThat(parent.getChildren()).isEmpty();

        final var childAssigned = parent
                .update(assign(child))
                .save().getDomain();

        assertThat(parent.getChildren()).containsExactly(child);
        assertThat(childAssigned.getVersion()).isEqualTo(2);
        assertThat(currentPersistedChild().getParentNaturalId())
                .isEqualTo(parentNaturalId);
        assertThat(events()).containsExactly(
                new ChildChangedEvent(
                        new ChildResource(childNaturalId, null, null,
                                emptySet(), 1),
                        new ChildResource(childNaturalId, parentNaturalId,
                                null, emptySet(), 2)),
                new ParentChangedEvent(
                        new ParentResource(parentNaturalId, null, 1),
                        new ParentResource(parentNaturalId, null, 2)));

        final var childUnassigned = parent
                .update(unassign(child))
                .save().getDomain();

        assertThat(parent.getChildren()).isEmpty();
        assertThat(childUnassigned.getVersion()).isEqualTo(3);
        assertThat(currentPersistedChild().getParentNaturalId()).isNull();
        assertThat(events()).containsExactly(
                new ChildChangedEvent(
                        new ChildResource(childNaturalId, parentNaturalId,
                                null, emptySet(), 2),
                        new ChildResource(childNaturalId, null, null,
                                emptySet(), 3)),
                new ParentChangedEvent(
                        new ParentResource(parentNaturalId, null, 2),
                        new ParentResource(parentNaturalId, null, 3)));
    }

    private Child newSavedChild() {
        final var saved = children.createNewUnassigned(childNaturalId).save();
        assertThat(saved.isChanged()).isTrue();
        final var child = saved.getDomain();
        testListener.reset();
        return child;
    }

    private Child currentPersistedChild() {
        return children.findExisting(childNaturalId).orElseThrow();
    }

    private Parent newSavedParent() {
        final var saved = parents.createNew(parentNaturalId).save();
        assertThat(saved.isChanged()).isTrue();
        final var parent = saved.getDomain();
        testListener.reset();
        return parent;
    }

    private Parent currentPersistedParent() {
        return parents.findExisting(parentNaturalId).orElseThrow();
    }

    private List<DomainChangedEvent<?>> events() {
        return testListener.events();
    }
}
