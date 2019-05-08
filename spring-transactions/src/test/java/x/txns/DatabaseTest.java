package x.txns;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;

@AutoConfigureEmbeddedDatabase
@DataJdbcTest
@Transactional
class DatabaseTest {
    @MockBean
    private Logger logger;
    @Autowired
    private FooRepository foos;

    @Test
    void shouldContinueAfterSavepointFails() {
        verify(logger).warn(
                startsWith("FAILED NESTED TRANSACTION"), anyString());

        assertThat(foos.count())
                .withFailMessage("Missing foo")
                .isEqualTo(1);
    }

    @TestConfiguration
    static class MyTestConfiguration {
        @Bean
        public RunIt runIt(
                final FooRepository foos,
                final FailedNestedTransaction nested,
                final ApplicationEventPublisher publisher,
                final Logger logger) {
            return new RunIt(foos, nested, publisher, logger);
        }

        @Bean
        public FailedNestedTransaction nested(
                final FooRepository foos, final Logger logger) {
            return new FailedNestedTransaction(foos, logger);
        }
    }
}
