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
import java.util.function.Predicate;
import java.util.stream.Stream;

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
class SallyDataTest {
    private final SallyRepository repository;
    private final HowardRepository howardRepository;
    private final SqlQueries queries;

    @LocalServerPort
    private int port;

    private static Predicate<String> sql(final String prefix) {
        return q -> q.startsWith(prefix);
    }

    private static boolean mutatingSql(final String q) {
        return q.startsWith("DELETE")
                || q.startsWith("INSERT")
                || q.startsWith("UPDATE");
    }

    @AfterEach
    void tearDown() {
        queries.clear();
    }

    @Test
    void shouldRoundTrip()
            throws IOException, InterruptedException {
        final var unsaved = new SallyRecord();
        unsaved.name = "William";
        final var saved = repository.save(unsaved);
        final var found = repository.findById(saved.id);

        assertThat(found).contains(saved);

        assertThat(queries).hasSize(3);
        assertThat(queries.get(0)).startsWith("INSERT");
        assertThat(queries.get(1)).startsWith("SELECT");
        assertThat(queries.get(2)).startsWith("SELECT");

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

    @Test
    void shouldUpdateWithoutDeletingChildren() {
        final var unsaved = new SallyRecord();
        unsaved.name = "William";
        final var saved = repository.save(unsaved);
        final var found = repository.findById(saved.id).get();
        final var howardA = new HowardRecord();
        howardA.name = "Howard A";
        final var nancyA = new NancyRecord();
        nancyA.name = "Nancy A";
        howardA.nancies.add(nancyA);
        howardRepository.save(howardA);
        final var howardB = new HowardRecord();
        howardB.name = "Howard B";
        final var nancyB = new NancyRecord();
        nancyB.name = "Nancy B";
        howardB.nancies.add(nancyB);
        howardRepository.save(howardB);
        final var howardC = new HowardRecord();
        howardC.name = "Howard C";
        final var nancyC = new NancyRecord();
        nancyC.name = "Nancy C";
        howardC.nancies.add(nancyC);
        howardRepository.save(howardC);
        final var howardD = new HowardRecord();
        howardC.name = "Howard D";
        howardRepository.save(howardD);
        final var nancyD = new NancyRecord();
        nancyD.name = "Nancy D";
        howardD.nancies.add(nancyD);

        queries.clear();
        found.howards.add(howardA.ref());
        repository.save(found);

        assertThat(mutatingQueries()).hasSize(3);
        assertThat(insertQueries()).hasSize(1);

        queries.clear();
        found.howards.add(howardB.ref());
        repository.save(found);

        assertThat(mutatingQueries()).hasSize(4);
        assertThat(insertQueries()).hasSize(2);

        queries.clear();
        found.howards.add(howardC.ref());
        repository.save(found);

        assertThat(mutatingQueries()).hasSize(5);
        assertThat(insertQueries()).hasSize(3);

        queries.clear();
        found.howards.add(howardD.ref());
        repository.save(found);

        assertThat(mutatingQueries()).hasSize(6);
        assertThat(insertQueries()).hasSize(4);
    }

    private Stream<String> insertQueries() {
        return queries.stream().filter(sql("INSERT"));
    }

    private Stream<String> mutatingQueries() {
        return queries.stream().filter(SallyDataTest::mutatingSql);
    }
}
