package hello.world.data;

import io.micronaut.test.annotation.MicronautTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@RequiredArgsConstructor
class FooRepositoryTest {
    private final FooRepository repository;

    @Disabled("Micronaut not running Flyway migrations")
    @Test
    void shouldRoundTrip() {
        final var unsaved = new FooRecord();
        unsaved.setCode("ABC");
        unsaved.setFoo("FOO");

        final var saved = repository.save(unsaved);

        assertEquals(unsaved, saved);

        final var found = repository.findById(unsaved.getCode()).get();

        assertEquals(found, saved);
    }
}
