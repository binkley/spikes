package x.loggy;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Test;
import x.loggy.AlertMessage.MessageFinder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static x.loggy.AlertMessage.MessageFinder.findAlertMessage;

class AlertMessageTest {
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
        assertThatThrownBy(TestyMethods::foo)
                .isInstanceOf(TestyException.class)
                .extracting(MessageFinder::findAlertMessage)
                .isEqualTo("FOO");
    }

    @Test
    void shouldFindNestedMessage() {
        assertThatThrownBy(TestyMethods::bar)
                .isInstanceOf(TestyException.class)
                .extracting(MessageFinder::findAlertMessage)
                .isEqualTo("QUX");
    }

    @UtilityClass
    static class TestyMethods {
        @AlertMessage("FOO")
        void foo() {
            throw new TestyException();
        }

        @AlertMessage("BAR")
        void bar() {
            try {
                NestedMethods.qux();
            } catch (final TestyException e) {
                throw new TestyException(e);
            }
        }

        @UtilityClass
        static class NestedMethods {
            @AlertMessage("QUX")
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
