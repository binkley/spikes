package x.scratch;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import x.scratch.child.Child;
import x.scratch.child.ChildFactory;
import x.scratch.child.ChildSnapshot;
import x.scratch.parent.Parent;
import x.scratch.parent.ParentFactory;
import x.scratch.parent.ParentSnapshot;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static x.scratch.parent.MutableParent.Helper.assign;

@AutoConfigureTestDatabase(replace = NONE)
@RequiredArgsConstructor
@SpringBootTest
@TestInstance(PER_CLASS)
@Transactional
public abstract class LiveTestBase {
    protected static final String parentNaturalId = "p";
    protected static final String childNaturalId = "c";

    protected final ParentFactory parents;
    protected final ChildFactory children;
    private final SqlQueries sqlQueries;
    private final TestListener<DomainChangedEvent<?>> testListener;

    protected static ChildSnapshotBuilder childSnapshot() {
        return new ChildSnapshotBuilder();
    }

    protected static ParentSnapshotBuilder parentSnapshot() {
        return new ParentSnapshotBuilder();
    }

    @AfterEach
    void liveTestBaseTearDown() {
        assertThat(sqlQueries.queriesByType()).isEmpty();
        assertThat(testListener.events()).isEmpty();
    }

    protected Map<String, List<String>> sqlQueriesByType() {
        return sqlQueries.queriesByType();
    }

    protected void assertSqlQueryTypesByCount(
            final Map<String, Integer> expectedCounts) {
        assertThat(sqlQueriesByType().entrySet().stream()
                .collect(toMap(Entry::getKey, e -> e.getValue().size())))
                .isEqualTo(expectedCounts);
    }

    protected List<DomainChangedEvent<?>> events() {
        return testListener.events();
    }

    protected void assertDomainChangedEvents(
            final DomainChangedEvent<?>... expectedEvents) {
        assertThat(events()).containsExactly(expectedEvents);
    }

    protected void assertEvents(
            final DomainChangedEvent<?>... expectedEvents) {
        assertThat(events()).containsExactly(expectedEvents);
    }

    protected ListAssert<Parent> assertAllParents() {
        final var assertion = assertThat(parents.all());
        sqlQueries.reset();
        return assertion;
    }

    protected Parent newUnsavedParent() {
        return parents.createNew(parentNaturalId);
    }

    protected Parent newSavedParent() {
        final var saved = newUnsavedParent().save();
        assertThat(saved.isChanged()).as("Parent already saved")
                .isTrue();
        sqlQueries.reset();
        testListener.reset();
        return saved.getDomain();
    }

    protected Parent currentPersistedParent() {
        final var existing = parents.findExisting(parentNaturalId);
        assertThat(existing).as("No saved parent")
                .isNotEmpty();
        sqlQueries.reset();
        return existing.get();
    }

    protected ListAssert<Child> assertAllChildren() {
        final var assertion = assertThat(children.all());
        sqlQueries.reset();
        return assertion;
    }

    protected Child newUnsavedUnassignedChild() {
        return children.createNewUnassigned(childNaturalId);
    }

    protected Child newSavedUnassignedChild() {
        final var saved = newUnsavedUnassignedChild().save();
        assertThat(saved.isChanged()).as("Child already saved")
                .isTrue();
        sqlQueries.reset();
        testListener.reset();
        return saved.getDomain();
    }

    protected ParentChild newSavedAssignedChild() {
        final var parentChild = new ParentChild();
        sqlQueries.reset();
        testListener.reset();
        return parentChild;
    }

    protected Child currentPersistedChild() {
        final var existing = children.findExisting(childNaturalId);
        assertThat(existing).as("No saved child")
                .isNotEmpty();
        sqlQueries.reset();
        return existing.get();
    }

    protected static final class ChildSnapshotBuilder {
        private final Set<String> subchildren = new LinkedHashSet<>();
        private String parentNaturalId = null;
        private String value = null;
        private int version = 0;

        public ChildSnapshotBuilder unassigned() {
            parentNaturalId = null;
            return this;
        }

        public ChildSnapshotBuilder assigned() {
            parentNaturalId = LiveTestBase.parentNaturalId;
            return this;
        }

        public ChildSnapshotBuilder value(final String value) {
            this.value = value;
            return this;
        }

        public ChildSnapshotBuilder subchildren(
                final Set<String> subchildren) {
            this.subchildren.clear();
            this.subchildren.addAll(subchildren);
            return this;
        }

        public ChildSnapshotBuilder version(final int version) {
            this.version = version;
            return this;
        }

        public ChildSnapshot build() {
            return new ChildSnapshot(childNaturalId,
                    parentNaturalId, value, subchildren, version);
        }
    }

    protected static final class ParentSnapshotBuilder {
        private String value = null;
        private int version = 0;

        public ParentSnapshotBuilder value(final String value) {
            this.value = value;
            return this;
        }

        public ParentSnapshotBuilder version(final int version) {
            this.version = version;
            return this;
        }

        public ParentSnapshot build() {
            return new ParentSnapshot(parentNaturalId, value, version);
        }
    }

    protected final class ParentChild {
        public final Parent parent;
        public final Child child;

        private ParentChild() {
            parent = newUnsavedParent();
            child = newUnsavedUnassignedChild();
            parent.update(assign(child)).save();
        }
    }
}
