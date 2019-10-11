package x.scratch;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class DatabaseTest {
    private final ParentRepository parents;

    @Test
    void shouldRoundTripParent() {
        final var unsaved = ParentRecord.builder()
                .naturalId("a")
                .build();
        final var saved = parents.upsert(unsaved);

        assertThat(saved).isEqualTo(unsaved);
        assertThat(saved.getVersion()).isEqualTo(1);
    }
}
