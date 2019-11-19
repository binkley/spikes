package x.scratch;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;
import x.scratch.child.Child;
import x.scratch.child.ChildFactory;
import x.scratch.parent.Parent;
import x.scratch.parent.ParentFactory;

import java.time.OffsetDateTime;

import static java.time.Instant.EPOCH;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(PER_CLASS)
@Transactional
public abstract class LiveTestBase {
    protected static final String parentNaturalId = "p";
    protected static final String childNaturalId = "c";
    protected static final OffsetDateTime atZero = EPOCH.atOffset(UTC);

    protected final ChildFactory children;
    protected final ParentFactory parents;
    protected final SqlQueries sqlQueries;
    protected final TestListener<?> testListener;

    @AfterEach
    void liveTestBaseTearDown() {
        assertThat(sqlQueries.queriesByType()).isEmpty();
        assertThat(testListener.events()).isEmpty();
    }

    protected Parent newUnsavedParent() {
        return parents.createNew(parentNaturalId);
    }

    protected Parent newSavedParent() {
        final var saved = newUnsavedParent().save();
        assertThat(saved.isChanged()).as("Duplicate upsert")
                .isTrue();
        sqlQueries.reset();
        testListener.reset();
        return saved.getDomain();
    }

    protected Parent currentPersistedParent() {
        final var existing = parents.findExisting(parentNaturalId);
        assertThat(existing).as("Never saved")
                .isNotEmpty();
        sqlQueries.reset();
        return existing.get();
    }

    protected Child newUnsavedUnassignedChild() {
        return children.createNewUnassigned(childNaturalId);
    }

    protected Child newSavedUnassignedChild() {
        final var saved = newUnsavedUnassignedChild().save();
        assertThat(saved.isChanged()).as("Duplicate upsert")
                .isTrue();
        sqlQueries.reset();
        testListener.reset();
        return saved.getDomain();
    }

    protected Child currentPersistedChild() {
        final var existing = children.findExisting(childNaturalId);
        assertThat(existing).as("Never saved")
                .isNotEmpty();
        sqlQueries.reset();
        return existing.get();
    }
}
