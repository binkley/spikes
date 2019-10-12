package x.scratch;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import x.scratch.UpsertableDomain.UpsertedDomainResult;

import static org.assertj.core.api.Assertions.assertThat;
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

        final var saved = unsaved.save();

        assertThat(unsaved.getVersion()).isEqualTo(1);
        assertThat(saved).isEqualTo(UpsertedDomainResult.of(unsaved, true));

        final var found = parents.findExisting(naturalId).orElseThrow();

        assertThat(found).isEqualTo(unsaved);
    }
}
