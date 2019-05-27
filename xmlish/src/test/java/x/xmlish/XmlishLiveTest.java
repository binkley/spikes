package x.xmlish;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import x.xmlish.ComplexExample.Body.BookReview.Table.Tr.Td;
import x.xmlish.SimpleExample.Bar;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static x.xmlish.ReadXml.readXml;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(PER_CLASS)
class XmlishLiveTest {
    private static final HttpClient client = HttpClient.newBuilder().build();

    private final ObjectMapper objectMapper;
    private final Validator validator;

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

        out.println(response.body());
    }

    @Test
    void shouldPost()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(readXml("good-xmlish")))
                .uri(URI.create(format("http://localhost:%d", port)))
                .header("Content-Type", "application/xml")
                .build();

        final var response = client.send(request, discarding());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldPostGoodComplex()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(readXml(
                        "good-complex-example")))
                .uri(URI.create(format("http://localhost:%d/complex", port)))
                .header("Content-Type", "application/xml")
                .build();

        final var response = client.send(request, discarding());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldPostBadComplex()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(readXml(
                        "bad-complex-example")))
                .uri(URI.create(format("http://localhost:%d/complex", port)))
                .header("Content-Type", "application/xml")
                .build();

        final var response = client.send(request, discarding());

        assertThat(response.statusCode()).isEqualTo(422);
    }

    @Test
    void shouldPostGoodSimple()
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(readXml(
                        "good-simple-example")))
                .uri(URI.create(format("http://localhost:%d/simple", port)))
                .header("Content-Type", "application/xml")
                .build();

        final var response = client.send(request, discarding());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldParseGoodComplexExample()
            throws IOException {
        final var name = "good-complex-example";
        final var complexExample = objectMapper.readValue(
                readXml(name),
                ComplexExample.class);

        final var errors = new BeanPropertyBindingResult(
                complexExample, name);
        validator.validate(complexExample, errors);

        assertThat(errors.getAllErrors()).isEmpty();

        assertThat(complexExample
                .getBody()
                .getBookreview()
                .getTable()
                .getTr().stream()
                .flatMap(tr -> tr.getTd().stream())
                .map(Td::getText))
                .containsExactly("Author", "Price", "Pages", "Date");
    }

    @Test
    void shouldComplainAboutBadComplexExample()
            throws IOException {
        final var name = "bad-complex-example";
        final var complexExample = objectMapper.readValue(
                readXml(name),
                ComplexExample.class);

        final var errors = new BeanPropertyBindingResult(
                complexExample, name);
        validator.validate(complexExample, errors);

        assertThat(errors.getAllErrors()).hasSize(1);
    }

    @Test
    void shouldParseGoodSimpleExample()
            throws IOException {
        final var name = "good-simple-example";
        final var simpleExample = objectMapper.readValue(
                readXml(name),
                SimpleExample.class);

        final var errors = new BeanPropertyBindingResult(
                simpleExample, name);
        validator.validate(simpleExample, errors);

        assertThat(errors.getAllErrors()).isEmpty();

        assertThat(simpleExample
                .getBar().stream()
                .map(Bar::getText))
                .containsExactly("Author", "Price");
    }
}
