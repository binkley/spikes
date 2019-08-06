package x.loggy;

import brave.Tracing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdsStarterTest {
    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldInjectMdcTraceIds() {
        final var tracing = Tracing.newBuilder().build();
        final var starter = new TraceIdsStarter(tracing);

        starter.newTraceIdsOnCurrentThread();

        assertThat(MDC.get("X-B3-Sampled")).isEqualTo("1");
        final var traceId = MDC.get("X-B3-TraceId");
        assertThat(traceId).isNotNull();
        assertThat(MDC.get("X-B3-SpanId")).isEqualTo(traceId);
    }
}
