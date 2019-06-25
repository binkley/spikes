package hello.world;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class HelloControllerSpec {
    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    HelloClient helloClient;

    @Test
    void testHelloWorldResponse() {
        final var response = client.toBlocking()
                .retrieve(HttpRequest.GET("/hello"));

        assertEquals("Hello World", response);
    }

    @Test
    void testHelloWorldResponse2() {
        final var response = helloClient.hello().blockingGet();

        assertEquals("Hello World", response);
    }
}
