package x;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AUnitTest {
    @Test
    void shouldDoIt() {
        final AUnit lib = new AUnit();
        final var name = "BOB";

        final var depends = new BUnit(name);

        assertThat(lib.doIt(depends)).contains(name);
    }
}
