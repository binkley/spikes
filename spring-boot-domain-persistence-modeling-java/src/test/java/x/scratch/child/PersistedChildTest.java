package x.scratch.child;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import x.scratch.TestListener;
import x.scratch.UpsertableDomain.UpsertedDomainResult;
import x.scratch.parent.Parent;
import x.scratch.parent.ParentFactory;

import java.util.List;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest
@TestInstance(PER_CLASS)
@Transactional
class PersistedChildTest {
    private static final String naturalId = "p";
    private static final String parentNaturalId = "a";

    private final ChildFactory children;
    private final ParentFactory parents;
    private final TestListener<ChildChangedEvent> testListener;

    @Test
    void shouldRoundTrip() {
        final var unsaved = children.createNew(naturalId);

        assertThat(unsaved.getVersion()).isEqualTo(0);
        assertThat(events()).isEmpty();

        final var saved = unsaved.save();

        assertThat(children.all()).hasSize(1);
        assertThat(unsaved.getVersion()).isEqualTo(1);
        assertThat(saved).isEqualTo(UpsertedDomainResult.of(unsaved, true));
        assertThat(events()).containsExactly(new ChildChangedEvent(
                null,
                new ChildResource(naturalId, null, null, emptySet(), 1)));

        final var found = children.findExisting(naturalId).orElseThrow();

        assertThat(found).isEqualTo(unsaved);
    }

    @Test
    void shouldDetectNoChanges() {
        final var original = newSavedChild();
        final var resaved = original.save();

        assertThat(resaved)
                .isEqualTo(UpsertedDomainResult.of(original, false));
        assertThat(events()).isEmpty();
    }

    @Test
    void shouldMutate() {
        final var original = newSavedChild();

        final var value = "FOOBAR";
        final var modified = original.update(it -> it.setValue(value));

        assertThat(modified).isEqualTo(original);
        assertThat(original.getValue()).isEqualTo(value);
        assertThat(events()).isEmpty();

        original.save();

        assertThat(events()).containsExactly(new ChildChangedEvent(
                new ChildResource(naturalId, null, null, emptySet(), 1),
                new ChildResource(naturalId, null, value, emptySet(), 2)));
    }

    @Test
    void shouldDelete() {
        final var existing = newSavedChild();

        existing.delete();

        assertThat(children.all()).isEmpty();
        assertThatThrownBy(existing::getVersion)
                .isInstanceOf(NullPointerException.class);
        assertThat(events()).containsExactly(new ChildChangedEvent(
                new ChildResource(naturalId, null, null, emptySet(), 1),
                null));
    }

    @Test
    void shouldAssignChildAtCreation() {
        final var parent = newSavedParent();

        assertThat(parent.getVersion()).isEqualTo(1);

        final var unsaved = children.createNew(naturalId)
                .update(it -> it.assignTo(parent));

        assertThat(unsaved.getParentNaturalId()).isEqualTo(parentNaturalId);
        assertThat(events()).isEmpty();

        unsaved.save();

        assertThat(unsaved.getVersion()).isEqualTo(1);
        assertThat(children.findExisting(naturalId).orElseThrow()
                .getParentNaturalId()).isEqualTo(parentNaturalId);
        assertThat(parents.findExisting(parentNaturalId).orElseThrow()
                .getVersion()).isEqualTo(2);
    }

    private Child newSavedChild() {
        final var child = children.createNew(naturalId).save().getDomain();
        testListener.reset();
        return child;
    }

    private Parent newSavedParent() {
        final var parent = parents.createNew(parentNaturalId).save()
                .getDomain();
        testListener.reset();
        return parent;
    }

    private List<ChildChangedEvent> events() {
        return testListener.events();
    }
}
