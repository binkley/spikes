package hello.world;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.retry.event.RetryEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@MicronautTest
class HelloControllerSpec {
    @Inject
    @SuppressWarnings({"UnusedVariable", "unused"})
    private EmbeddedServer server;
    @Inject
    @Client("/")
    private HttpClient client;
    @Inject
    private HelloClient helloClient;
    @Inject
    private RetryEventListener retryEventListener;
    @Inject
    private Validator validator;

    @BeforeEach
    void setUp() {
        retryEventListener.events.clear();
    }

    @Test
    void testHelloWorldResponse() {
        final var response = client.toBlocking().retrieve(GET("/hello"));

        assertEquals("Hello World", response);
    }

    @Test
    void testHelloWorldResponse2() {
        final var response = helloClient.hello().blockingGet();

        assertEquals("Hello World", response);
    }

    @Test
    void testDataValidation() {
        final var sampleData = new RoundTripData();
        sampleData.setA("");
        sampleData.setB(-3);

        final var result = validator.validate(sampleData);

        assertFalse(result.isEmpty());
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    @Test
    void testControllerValidation() {
        final var sampleData = new RoundTripData();
        sampleData.setA("");
        sampleData.setB(-3);

        try {
            helloClient.roundTrip(sampleData).blockingGet();
            fail();
        } catch (final HttpClientResponseException e) {
            assertEquals(BAD_REQUEST, e.getStatus());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testRetry() {
        try {
            helloClient.notThere().blockingGet();
            fail();
        } catch (final HttpClientResponseException e) {
            assertEquals(NOT_FOUND, e.getStatus());
            assertEquals(3, retryEventListener.events.size());
        }
    }

    @Singleton
    public static class RetryEventListener {
        private final List<RetryEvent> events = new ArrayList<>();

        @EventListener
        void onRetry(final RetryEvent event) {
            events.add(event);
        }
    }
}
