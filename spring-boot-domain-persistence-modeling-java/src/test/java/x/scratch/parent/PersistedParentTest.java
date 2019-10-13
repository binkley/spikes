package x.scratch.parent;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import x.scratch.TestListener;
import x.scratch.UpsertableDomain.UpsertedDomainResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest
@TestInstance(PER_CLASS)
@Transactional
class PersistedParentTest {
    private static final String naturalId = "a";

    private final PersistedParentFactory parents;
    private final TestListener<ParentChangedEvent> testListener;

    @Test
    void shouldRoundTrip() {
        final var unsaved = parents.createNew(naturalId);

        assertThat(unsaved.getVersion()).isEqualTo(0);
        assertThat(events()).isEmpty();

        final var saved = unsaved.save();

        assertThat(parents.all()).hasSize(1);
        assertThat(unsaved.getVersion()).isEqualTo(1);
        assertThat(saved).isEqualTo(UpsertedDomainResult.of(unsaved, true));
        assertThat(events()).containsExactly(new ParentChangedEvent(
                null,
                new ParentResource(naturalId, null, 1)));

        final var found = parents.findExisting(naturalId).orElseThrow();

        assertThat(found).isEqualTo(unsaved);
    }

    @Test
    void shouldDetectNoChanges() {
        final var original = newSavedParent();
        final var resaved = original.save();

        assertThat(resaved)
                .isEqualTo(UpsertedDomainResult.of(original, false));
        assertThat(events()).isEmpty();
    }

    @Test
    void shouldMutate() {
        final var original = newSavedParent();

        final var modified = original.update(it -> {
            it.setValue("FOOBAR");
        });

        assertThat(modified).isEqualTo(original);
        assertThat(original.getValue()).isEqualTo("FOOBAR");
        assertThat(events()).isEmpty();
    }

    @Test
    void shouldDelete() {
        final var existing = newSavedParent();

        existing.delete();

        assertThat(parents.all()).isEmpty();
        assertThatThrownBy(existing::getVersion)
                .isInstanceOf(NullPointerException.class);
        assertThat(events()).containsExactly(new ParentChangedEvent(
                new ParentResource(naturalId, null, 1),
                null));
    }

    private Parent newSavedParent() {
        final var parent = parents.createNew(naturalId).save().getDomain();
        testListener.reset();
        return parent;
    }

    private List<ParentChangedEvent> events() {
        return testListener.events();
    }
}
