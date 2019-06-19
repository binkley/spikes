package x;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AUnitTest {
    @Test
    void testSomeLibraryMethod() {
        final Library lib = new Library();
        final var depends = new DependOnMe("BOB");

        assertThat(lib.someLibraryMethod(depends))
                .withFailMessage("someLibraryMethod should return 'true'")
                .isTrue();
    }
}
