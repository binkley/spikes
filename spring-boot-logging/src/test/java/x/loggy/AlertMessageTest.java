package x.loggy;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static x.loggy.AlertMessage.MessageFinder.findAlertMessage;

class AlertMessageTest {
    private final TestyMethods testyMethods = new TestyMethods();

    private static void assertThatAlertMessage(final Throwable t,
            final String message) {
        final var alertMessage = findAlertMessage(t);
        assertThat(alertMessage).isNotNull();
        assertThat(alertMessage.message()).isEqualTo(message);
    }

    @Test
    void shouldFindNoMessage() {
        assertThat(findAlertMessage(new TestyException())).isNull();
    }

    @Test
    void shouldFindNoMessageNested() {
        assertThat(findAlertMessage(
                new TestyException(new TestyException()))).isNull();
    }

    @Test
    void shouldFindTopLevelMessage() {
        assertThatThrownBy(testyMethods::foo)
                .isInstanceOf(TestyException.class)
                .satisfies(t -> assertThatAlertMessage(t, "FOO"));
    }

    @Test
    void shouldFindNestedMessage() {
        assertThatThrownBy(testyMethods::bar)
                .isInstanceOf(TestyException.class)
                .satisfies(t -> assertThatAlertMessage(t, "QUX"));
    }

    @Test
    void shouldFindInterfaceMessage() {
        assertThatThrownBy(testyMethods::baz)
                .isInstanceOf(TestyException.class)
                .satisfies(t -> assertThatAlertMessage(t, "BAZ"));
    }

    interface Bazable {
        @AlertMessage(message = "BAZ")
        void baz();
    }

    static class TestyMethods
            implements Bazable {
        @AlertMessage(message = "FOO")
        void foo() {
            throw new TestyException();
        }

        @AlertMessage(message = "BAR")
        void bar() {
            try {
                NestedMethods.qux();
            } catch (final TestyException e) {
                throw new TestyException(e);
            }
        }

        public void baz() {
            throw new TestyException();
        }

        @UtilityClass
        static class NestedMethods {
            @AlertMessage(message = "QUX")
            void qux() {
                throw new TestyException();
            }
        }
    }

    public static final class TestyException
            extends RuntimeException {
        public TestyException() { }

        public TestyException(final TestyException nested) {
            super(nested);
        }
    }
}
