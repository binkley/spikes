package x.loggy;

import brave.Tracing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraceIdsStarterTest {
    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldInjectMdcTraceIds() {
        final var tracing = Tracing.newBuilder().build();
        final var starter = new TraceIdsStarter(tracing);

        final var expectedContext = starter.newTraceIdsOnCurrentThread();

        final var actualContext = tracing
                .propagation()
                .extractor(MDCAdapter::get)
                .extract(MDC.getMDCAdapter())
                .context();

        assertThat(actualContext).isEqualTo(expectedContext);
    }

    @Test
    void shouldComplainIfThereAreAlreadyTraceIdsInMdc() {
        final var tracing = Tracing.newBuilder().build();
        final var starter = new TraceIdsStarter(tracing);

        starter.newTraceIdsOnCurrentThread();

        assertThatThrownBy(starter::newTraceIdsOnCurrentThread)
                .isInstanceOf(Bug.class);
    }
}
