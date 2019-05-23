package x.loggy;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import x.loggy.AlertMessage.MessageFinder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Instant;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ActiveProfiles("json")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(properties = {
        "logging.level.x.loggy=WARN",
        "loggy.enable-demo=false"
}, webEnvironment = DEFINED_PORT)
class AlertLiveTest {
    private static final HttpClient client = HttpClient.newBuilder().build();

    private final ConflictRemote conflict;

    @MockBean(name = "logger")
    private Logger logger;
    @MockBean(name = "httpLogger")
    private Logger httpLogger;

    @BeforeEach
    void setUp() {
        lenient().when(logger.isTraceEnabled()).thenReturn(true);
        lenient().when(logger.isDebugEnabled()).thenReturn(true);
        lenient().when(logger.isInfoEnabled()).thenReturn(true);
        lenient().when(logger.isWarnEnabled()).thenReturn(true);
        lenient().when(logger.isErrorEnabled()).thenReturn(true);
        lenient().when(httpLogger.isTraceEnabled()).thenReturn(true);
        lenient().when(httpLogger.isDebugEnabled()).thenReturn(true);
        lenient().when(httpLogger.isInfoEnabled()).thenReturn(true);
        lenient().when(httpLogger.isWarnEnabled()).thenReturn(true);
        lenient().when(httpLogger.isErrorEnabled()).thenReturn(true);
    }

    @Test
    void shouldAlertThroughWebDirectly()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/npe"))
                .build();

        final var response = sendAndDiscardBody(request);

        assertThat(response.statusCode()).isEqualTo(500);

        verify(logger).error(anyString(), eq("NULLITY"));
    }

    private static HttpResponse<Void> sendAndDiscardBody(
            final HttpRequest request)
            throws IOException, InterruptedException {
        return client.send(request, discarding());
    }

    @Test
    void shouldAlertThroughFeignIndirectly()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(noBody())
                .uri(URI.create("http://localhost:8080/conflict"))
                .build();

        final var response = sendAndDiscardBody(request);

        assertThat(response.statusCode()).isEqualTo(502);

        verify(logger).error(anyString(), eq("CONFLICTED"));
    }

    @Test
    void givenAlertDirectly() {
        assertThatThrownBy(conflict::postConflict)
                .isInstanceOf(FeignException.class)
                .extracting(MessageFinder::findAlertMessage)
                .isEqualTo("CONFLICTED");
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
