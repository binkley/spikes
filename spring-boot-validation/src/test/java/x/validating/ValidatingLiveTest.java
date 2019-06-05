package x.validating;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.zalando.problem.Problem;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import static java.lang.String.format;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static x.validating.ReadJson.readJson;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(PER_CLASS)
class ValidatingLiveTest {
    private static final HttpClient client = HttpClient.newBuilder().build();

    private final ObjectMapper objectMapper;

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
    }

    @Test
    void shouldPostGood()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(readJson("good-validish")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, discarding());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldNotPostBad()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(readJson("bad-validish")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(422);

        final var body = response.body();
        System.out.println("body = " + body);
        final var problem = objectMapper.readValue(body, Problem.class);
        System.out.println("problem = " + problem);
    }

    @Test
    void shouldPutGood()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .PUT(BodyPublishers.ofString(readJson("good-validish")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, discarding());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldNotPutBad()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .PUT(BodyPublishers.ofString(readJson("bad-validish")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(422);

        final var body = response.body();
        System.out.println("body = " + body);
        final var problem = objectMapper.readValue(body, Problem.class);
        System.out.println("problem = " + problem);
    }
}
