package x.scratch;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(PER_CLASS)
@Transactional
class DatabaseTest {
    private static final String parentNaturalId = "a";
    private static final String childNaturalId = "p";

    private final ParentRepository parents;
    private final ChildRepository children;

    @Test
    void shouldRoundTripParent() {
        final var unsaved = newUnsavedParent();
        final var saved = parents.upsert(unsaved);

        assertThat(saved).isEqualTo(unsaved);
        assertThat(saved.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldNotBumpVersionOnParentWithoutChanges() {
        final var saved = newSavedParent();
        final var resaved = parents.upsert(saved);

        assertThat(resaved.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldRoundTripChild() {
        final var unsaved = newUnsavedChild();
        final var saved = children.upsert(unsaved);

        assertThat(saved).isEqualTo(unsaved);
        assertThat(saved.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldNotBumpVersionOnChildWithoutChanges() {
        final var saved = newSavedChild();
        final var resaved = children.upsert(saved);

        assertThat(resaved.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldAssignChildWhenCreating() {
        newSavedParent();

        final var savedChild = children.upsert(newUnsavedChild().toBuilder()
                .parentNaturalId(parentNaturalId)
                .build());

        assertThat(savedChild.getVersion()).isEqualTo(1);
        assertThat(findExistingParent().getVersion()).isEqualTo(2);
    }

    @Test
    void shouldAssignChildWhenModifying() {
        newSavedParent();

        final var savedChild = children.upsert(newUnsavedChild());

        savedChild.setParentNaturalId(parentNaturalId);
        final var updatedChild = children.upsert(savedChild);

        assertThat(updatedChild.getVersion()).isEqualTo(2);
        assertThat(findExistingParent().getVersion()).isEqualTo(2);
    }

    private ParentRecord newSavedParent() {
        return parents.upsert(newUnsavedParent());
    }

    private ChildRecord newSavedChild() {
        return children.upsert(newUnsavedChild());
    }

    private ParentRecord findExistingParent() {
        return parents.findByNaturalId(parentNaturalId).orElseThrow();
    }

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
}
