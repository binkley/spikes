package hello.world;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.retry.event.RetryEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@MicronautTest
class HelloControllerSpec {
    @Inject
    @SuppressWarnings("UnusedVariable")
    private EmbeddedServer server;
    @Inject
    @Client("/")
    private HttpClient client;
    @Inject
    private HelloClient helloClient;
    @Inject
    private RetryEventListener retryEventListener;

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

    @SuppressWarnings("CheckReturnValue")
    @Test
    void testValidation() {
        final var sampleData = new SampleData();
        sampleData.setA("");
        sampleData.setB(-3);

        try {
            helloClient.roundtrip(sampleData).blockingGet();
            fail();
        } catch (final HttpClientResponseException e) {
            assertEquals(BAD_REQUEST, e.getStatus());
        }
    }

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
