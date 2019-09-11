package x.loggy.data;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import static java.lang.String.format;
import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(properties = {
        "logging.level.x.loggy=WARN",
        "loggy.enable-demo=false"
}, webEnvironment = RANDOM_PORT)
class BobDataTest {
    private final BobRepository repository;
    private final SqlQueries queries;

    @LocalServerPort
    private int port;

    @AfterEach
    void tearDown() {
        queries.clear();
    }

    @Test
    void shouldRoundTrip()
            throws IOException, InterruptedException {
        final var unsaved = new BobRecord();
        unsaved.name = "William";
        final var saved = repository.save(unsaved);
        final var found = repository.findById(saved.id);

        assertThat(found).contains(saved);

        assertThat(queries).hasSize(2);
        assertThat(queries.get(0)).startsWith("INSERT");
        assertThat(queries.get(1)).startsWith("SELECT");

        final var client = newHttpClient();
        final var response = client.send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(format(
                        "http://localhost:%d/actuator/prometheus", port)))
                .build(), BodyHandlers.ofString(UTF_8));

        assertThat(response.body())
                .contains("database_calls_total{sql=\"select\"")
                .contains("database_calls_total{sql=\"insert\"");
    }
}
