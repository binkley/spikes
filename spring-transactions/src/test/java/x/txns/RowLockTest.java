package x.txns;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureEmbeddedDatabase
@DataJdbcTest
@Import(FooUpdater.class)
// @Transactional <-- Important NOT to make test transactional
class RowLockTest {
    @Autowired
    private FooRepository foos;
    @Autowired
    private FooUpdater updater;

    @AfterEach
    @BeforeEach
    void cleanUp() {
        foos.deleteAll();
    }

    @Test
    void shouldSerializeAccess()
            throws InterruptedException {
        final var value = new Random().nextInt();
        final var threads = newFixedThreadPool(2);

        foos.save(new FooRecord(null, "BAR", value));

        assertThat(threads.<Boolean>invokeAll(List.of(
                () -> updater.updateFoo(value),
                () -> updater.updateFoo(value)))
                .stream()
                .map(this::get))
                .containsExactlyInAnyOrder(true, false);
    }

    private Boolean get(final Future<Boolean> r) {
        try {
            return r.get();
        } catch (final Exception e) {
            final var x = new RuntimeException(e);
            x.setStackTrace(e.getStackTrace());
            throw x;
        }
    }
}
