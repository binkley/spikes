package x;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryTest {
    @Test
    void testSomeLibraryMethod() {
        final Library classUnderTest = new Library();

        assertThat(classUnderTest.someLibraryMethod())
                .withFailMessage("someLibraryMethod should return 'true'")
                .isTrue();
    }
}
