package x.scratch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import x.scratch.child.ChildFactory;
import x.scratch.parent.ParentFactory;

import java.util.Map;
import java.util.NoSuchElementException;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static x.scratch.parent.MutableParent.Helper.assign;

class PersistencePerformanceTest
        extends LiveTestBase {
    @Autowired
    PersistencePerformanceTest(final ParentFactory parents,
            final ChildFactory children,
            final SqlQueries sqlQueries,
            final TestListener<DomainChangedEvent<?>> testListener) {
        super(parents, children, sqlQueries, testListener);
    }

    private static <T> T first(final Iterable<T> c) {
        final var it = c.iterator();
        if (!it.hasNext()) throw new NoSuchElementException();
        return it.next();
    }

    @Test
    void shouldSaveOnlyOneMutatedChild() {
        final var parent = newSavedParent();
        for (int i = 0; i < 10; ++i)
            parent.update(assign(
                    children.createNewUnassigned(format("c%d", i))));
        parent.save();

        // 10 UPSERTS for children (no parent change); 1 SELECT for parent
        assertSqlQueryTypesByCount(Map.of("SELECT", 1, "UPSERT", 10));
        assertThat(events()).isNotEmpty();

        first(parent.getChildren()).update(it -> it.setValue("ABC"));
        parent.save();

        assertSqlQueryTypesByCount(Map.of("SELECT", 1, "UPSERT", 1));
        assertThat(events()).isNotEmpty();
    }
}
