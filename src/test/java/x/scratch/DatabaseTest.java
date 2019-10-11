package x.scratch;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(PER_CLASS)
class DatabaseTest {
    private final ParentRepository parents;
    private final ChildRepository children;

    @Test
    void shouldRoundTripParent() {
        final var unsaved = ParentRecord.builder()
                .naturalId("a")
                .build();
        final var saved = parents.upsert(unsaved);

        assertThat(saved).isEqualTo(unsaved);
        assertThat(saved.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldRoundTripChild() {
        final var unsaved = ChildRecord.builder()
                .naturalId("p")
                .build();
        final var saved = children.upsert(unsaved);

        assertThat(saved).isEqualTo(unsaved);
        assertThat(saved.getVersion()).isEqualTo(1);
    }
}
