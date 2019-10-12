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
        final var saved = parents.upsert(unsaved);

        assertThat(saved.getRecord()).isEqualTo(unsaved);
        assertThat(saved.getRecord().getVersion()).isEqualTo(1);
        assertThat(saved.isChanged()).isTrue();
    }

    @Test
    void shouldNotBumpVersionOnParentWithoutChanges() {
        final var saved = newSavedParent();
        final var resaved = parents.upsert(saved);

        assertThat(resaved.getRecord().getVersion()).isEqualTo(1);
        assertThat(resaved.isChanged()).isFalse();
    }

    @Test
    void shouldRoundTripChild() {
        final var unsaved = newUnsavedChild();
        final var saved = children.upsert(unsaved);

        assertThat(saved.getRecord()).isEqualTo(unsaved);
        assertThat(saved.getRecord().getVersion()).isEqualTo(1);
        assertThat(saved.isChanged()).isTrue();
    }

    @Test
    void shouldNotBumpVersionOnChildWithoutChanges() {
        final var saved = newSavedChild();
        final var resaved = children.upsert(saved);

        assertThat(resaved.getRecord().getVersion()).isEqualTo(1);
        assertThat(resaved.isChanged()).isFalse();
    }

    @Test
    void shouldAssignChildWhenCreating() {
        newSavedParent();

        final var savedChild = children.upsert(newUnsavedChild().toBuilder()
                .parentNaturalId(parentNaturalId)
                .build());

        assertThat(savedChild.getRecord().getVersion()).isEqualTo(1);
        assertThat(findExistingParent().getVersion()).isEqualTo(2);
    }

    @Test
    void shouldAssignChildWhenModifying() {
        newSavedParent();

        final var savedChild = children.upsert(newUnsavedChild());

        assertThat(savedChild.getRecord().getVersion()).isEqualTo(1);

        savedChild.getRecord().setParentNaturalId(parentNaturalId);
        final var updatedChild = children.upsert(savedChild.getRecord());

        assertThat(updatedChild.getRecord().getVersion()).isEqualTo(2);
        assertThat(findExistingParent().getVersion()).isEqualTo(2);
    }

    private ParentRecord newSavedParent() {
        return parents.upsert(newUnsavedParent()).getRecord();
    }

    private ChildRecord newSavedChild() {
        return children.upsert(newUnsavedChild()).getRecord();
    }

    private ParentRecord findExistingParent() {
        return parents.findByNaturalId(parentNaturalId).orElseThrow();
    }
}
