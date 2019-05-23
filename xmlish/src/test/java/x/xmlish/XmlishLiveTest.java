package x.xmlish;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(PER_CLASS)
class XmlishLiveTest {
    private static final HttpClient client = HttpClient.newBuilder().build();

    @LocalServerPort
    private int port;

    @Test
    void shouldGet()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(format("http://localhost:%d", port)))
                .build();

        final var response = client.send(
                request, BodyHandlers.ofString(UTF_8));

        System.out.println("response = " + response);
        System.out.println("response = " + response.body());
    }
}
