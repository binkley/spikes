package hm.binkley;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class MockitoStaticsTest {
    @Test
    void shouldRun() {
        try (final var mocked = mockStatic(X.class, invocation -> 1)) {
            assertEquals(1, X.aStaticMethod("abc"));
        }
    }

    public static class X {
        public static int aStaticMethod(final String x) {
            return x.length();
        }
    }
}
