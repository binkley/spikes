package x;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AUnitTest {
    @Test
    void shouldDoIt() {
        final AProdUnit lib = new AProdUnit();
        final var name = "BOB";

        final var depends = new DependOnMe(name);

        assertThat(lib.doIt(depends)).contains(name);
    }
}
