package hello.world;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class HelloControllerSpec {
    @Inject
    private EmbeddedServer server;
    @Inject
    @Client("/")
    private HttpClient client;
    @Inject
    private HelloClient helloClient;

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
    void testValidation() {
        final var sampleData = new SampleData();
        sampleData.setA("");
        sampleData.setB(-3);

        try {
            helloClient.roundtrip(sampleData).blockingGet();
        } catch (final HttpClientResponseException e) {
            assertEquals(BAD_REQUEST, e.getStatus());
        }
    }
}
