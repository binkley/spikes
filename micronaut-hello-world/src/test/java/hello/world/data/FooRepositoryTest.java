package hello.world.data;

import io.micronaut.test.annotation.MicronautTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@MicronautTest
@RequiredArgsConstructor
class FooRepositoryTest {
    private final FooRepository repository;

    @Test
    void should() {}
}
