package x;

import org.junit.jupiter.api.Test;

import static x.BeAssertive.assertLittle;
import static x.BeIttyAssertive.assertNothing;

class AIttyTest {
    @Test
    void shouldRhumba() {
        final var lib = new AUnit();

        assertLittle(lib);
        assertNothing();
    }
}
