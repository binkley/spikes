package x.loggy;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static x.loggy.AlertAssertions.assertThatAlertMessage;
import static x.loggy.AlertMessage.MessageFinder.findAlertMessage;
import static x.loggy.AlertMessage.Severity.HIGH;
import static x.loggy.AlertMessage.Severity.LOW;
import static x.loggy.AlertMessage.Severity.MEDIUM;

class AlertMessageTest {
    private final TestyMethods testyMethods = new TestyMethods();

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
                .satisfies(t -> assertThatAlertMessage(t, "FOO", HIGH));
    }

    @Test
    void shouldFindNestedMessage() {
        assertThatThrownBy(testyMethods::bar)
                .isInstanceOf(TestyException.class)
                .satisfies(t -> assertThatAlertMessage(t, "QUX", MEDIUM));
    }

    @Test
    void shouldFindInterfaceMessage() {
        assertThatThrownBy(testyMethods::baz)
                .isInstanceOf(TestyException.class)
                .satisfies(t -> assertThatAlertMessage(t, "BAZ", LOW));
    }

    interface Bazable {
        @AlertMessage(message = "BAZ", severity = LOW)
        void baz();
    }

    static class TestyMethods
            implements Bazable {
        @AlertMessage(message = "FOO", severity = HIGH)
        void foo() {
            throw new TestyException();
        }

        @AlertMessage(message = "BAR", severity = LOW)
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
            @AlertMessage(message = "QUX", severity = MEDIUM)
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
