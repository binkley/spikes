package x.xmlish;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import x.xmlish.Xmlish.Inner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;

import static java.lang.String.format;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(PER_CLASS)
class XmlishLiveTest {
    private static final HttpClient client = HttpClient.newBuilder().build();

    private final XmlMapper xmlMapper;

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

        assertThat(response.statusCode()).isEqualTo(200);

        System.out.println("response = " + response.body());
    }

    @Test
    void shouldPost()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(xmlMapper.writeValueAsString(
                        Xmlish.builder()
                                .foo("HI, MOM!")
                                .barNone(22)
                                .when(Instant.now())
                                .inner(Inner.builder()
                                        .qux("BYE, DAD!")
                                        .quux(77)
                                        .ever(Instant.now().minus(
                                                1_000_000L, SECONDS))
                                        .build())
                                .build())))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/xml")
                .build();

        final var response = client.send(request, discarding());

        assertThat(response.statusCode()).isEqualTo(200);
    }
}
