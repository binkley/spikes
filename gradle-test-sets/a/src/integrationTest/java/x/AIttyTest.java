package x;

import org.junit.jupiter.api.Test;

import static x.BeAssertive.assertLittle;

class AIttyTest {
    @Test
    void shouldRhumba() {
        final var lib = new Library();

        assertLittle(lib);
    }
}
