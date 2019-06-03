package x.loggy;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Test;
import x.loggy.AlertMessage.MessageFinder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static x.loggy.AlertMessage.MessageFinder.findAlertMessage;

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
                .extracting(MessageFinder::findAlertMessage)
                .isEqualTo("FOO");
    }

    @Test
    void shouldFindNestedMessage() {
        assertThatThrownBy(testyMethods::bar)
                .isInstanceOf(TestyException.class)
                .extracting(MessageFinder::findAlertMessage)
                .isEqualTo("QUX");
    }

    @Test
    void shouldFindInterfaceMessage() {
        assertThatThrownBy(testyMethods::baz)
                .isInstanceOf(TestyException.class)
                .extracting(MessageFinder::findAlertMessage)
                .isEqualTo("BAZ");
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
