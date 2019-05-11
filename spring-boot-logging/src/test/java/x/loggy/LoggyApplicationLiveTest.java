package x.loggy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(properties = {
        "loggy.enabled=false"
}, webEnvironment = DEFINED_PORT)
class LoggyApplicationLiveTest {
    private final Clock clock;
    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;

    @Test
    void shouldSendTracingThroughFeign() {
        MDC.put("X-B3-TraceId", "abcdef0987654321");
        MDC.put("X-B3-SpanId", "abcdef0987654321");
        MDC.put("X-B3-ParentSpanId", "abcdef0987654321");

        final var response = loggy.get();

        assertThat(response).isEqualTo(
                new LoggyResponse("HI, MOM!", 22, Instant.now(clock)));
    }

    @Test
    void shouldHandleNotFound() {
        MDC.put("X-B3-TraceId", "abcdef0987654321");
        MDC.put("X-B3-SpanId", "abcdef0987654321");
        MDC.put("X-B3-ParentSpanId", "abcdef0987654321");

        assertThatThrownBy(notFound::get)
                .hasFieldOrPropertyWithValue("status", 404);
    }

    @TestConfiguration
    public static class MyTestConfiguration {
        @Bean
        @Primary
        public Clock testClock() {
            return Clock.fixed(Instant.ofEpochSecond(1_000_000L), UTC);
        }
    }
}
