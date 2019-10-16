package x.scratch;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;
import x.scratch.child.ChildRecord;
import x.scratch.child.ChildRepository;
import x.scratch.parent.ParentRecord;
import x.scratch.parent.ParentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(PER_CLASS)
@Transactional
class PersistenceTest {
    private static final String parentNaturalId = "a";
    private static final String childNaturalId = "p";

    private final ParentRepository parents;
    private final ChildRepository children;

    private static ParentRecord newUnsavedParent() {
        return ParentRecord.builder()
                .naturalId(parentNaturalId)
                .build();
    }

    private static ChildRecord newUnsavedChild() {
        return ChildRecord.builder()
                .naturalId(childNaturalId)
                .build();
    }

    @Test
    void shouldRoundTripParent() {
        final var unsaved = newUnsavedParent();

        assertThat(unsaved.getVersion()).isEqualTo(0);

        final var saved = parents.upsert(unsaved).orElseThrow();

        assertThat(unsaved.getVersion()).isEqualTo(1);
        assertThat(saved).isEqualTo(unsaved);
    }

    @Test
    void shouldDetectNoChangesInParent() {
        final var saved = newSavedParent();

        assertThat(saved.getVersion()).isEqualTo(1);

        final var resaved = parents.upsert(saved);

        assertThat(resaved).isEmpty();
        assertThat(saved.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldRoundTripChild() {
        final var unsaved = newUnsavedChild();

        assertThat(unsaved.getVersion()).isEqualTo(0);

        final var saved = children.upsert(unsaved).orElseThrow();

        assertThat(unsaved.getVersion()).isEqualTo(1);
        assertThat(saved).isEqualTo(unsaved);
    }

    @Test
    void shouldDetectNoChangesInChild() {
        final var saved = newSavedChild();

        assertThat(saved.getVersion()).isEqualTo(1);

        final var resaved = children.upsert(saved);

        assertThat(resaved).isEmpty();
        assertThat(saved.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldAssignChildWhenCreating() {
        newSavedParent();

        final var savedChild = children.upsert(newUnsavedChild().toBuilder()
                .parentNaturalId(parentNaturalId)
                .build())
                .orElseThrow();

        assertThat(savedChild.getVersion()).isEqualTo(1);
        assertThat(findExistingParent().getVersion()).isEqualTo(2);
        assertThat(children.findByParentNaturalId(parentNaturalId))
                .containsExactly(savedChild);
    }

    @Test
    void shouldAssignChildWhenModifying() {
        newSavedParent();

        final var savedChild = newSavedChild();

        assertThat(savedChild.getVersion()).isEqualTo(1);

        savedChild.setParentNaturalId(parentNaturalId);
        final var updatedChild = children.upsert(savedChild)
                .orElseThrow();

        assertThat(updatedChild.getVersion()).isEqualTo(2);
        assertThat(findExistingParent().getVersion()).isEqualTo(2);
        assertThat(children.findByParentNaturalId(parentNaturalId))
                .containsExactly(savedChild);
    }

    private ParentRecord newSavedParent() {
        return parents.upsert(newUnsavedParent()).orElseThrow();
    }

    private ChildRecord newSavedChild() {
        return children.upsert(newUnsavedChild()).orElseThrow();
    }

    private ParentRecord findExistingParent() {
        return parents.findByNaturalId(parentNaturalId).orElseThrow();
    }
}
