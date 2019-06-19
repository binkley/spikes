package x;

import lombok.experimental.UtilityClass;

import static org.assertj.core.api.Assertions.assertThat;

@UtilityClass
public class BeAssertive {
    void assertLittle(final Object o) {
        assertThat(o).isNotNull();
    }
}
