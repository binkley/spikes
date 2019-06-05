package x.validating;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.zalando.problem.violations.ConstraintViolationProblem;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import static java.lang.String.format;
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

        final var response = client.send(request, BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        final var validish = objectMapper
                .readValue(response.body(), Validish.class);
        assertThat(validish.getInners().get(0).getQux()).isEqualTo("DUCK");
    }

    @Test
    void shouldNotPostBadNoInners()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(readJson(
                        "bad-validish-no-inners")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(422);

        final var problem = objectMapper
                .readValue(response.body(), ConstraintViolationProblem.class);
        final var violations = problem.getViolations();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).getField()).isEqualTo("inners");
    }

    @Test
    void shouldNotPostBadNoQux()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(readJson(
                        "bad-validish-no-qux")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(422);

        final var problem = objectMapper
                .readValue(response.body(), ConstraintViolationProblem.class);
        final var violations = problem.getViolations();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).getField()).isEqualTo("inners[0].qux");
    }

    @Test
    void shouldPutGood()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .PUT(BodyPublishers.ofString(readJson("good-validish")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        final var validish = objectMapper
                .readValue(response.body(), Validish.class);
        assertThat(validish.getInners().get(0).getQux()).isEqualTo("DUCK");
    }

    @Test
    void shouldNotPutBadNoInners()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .PUT(BodyPublishers.ofString(readJson(
                        "bad-validish-no-inners")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(422);

        final var problem = objectMapper
                .readValue(response.body(), ConstraintViolationProblem.class);
        final var violations = problem.getViolations();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).getField()).isEqualTo("inners");
    }

    @Test
    void shouldNotPutBadNoQux()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .PUT(BodyPublishers.ofString(readJson(
                        "bad-validish-no-qux")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/json")
                .build();

        final var response = client.send(request, BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(422);

        final var problem = objectMapper
                .readValue(response.body(), ConstraintViolationProblem.class);
        final var violations = problem.getViolations();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).getField()).isEqualTo("inners[0].qux");
    }
}
