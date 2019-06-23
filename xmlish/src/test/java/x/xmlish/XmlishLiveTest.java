package x.xmlish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.validation.Validator;
import x.xmlish.Outer.Inner;
import x.xmlish.Outer.Upper;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import static java.lang.String.format;
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
    private final JaxbMapper jaxbMapper = new JaxbMapper();

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
    void shouldParseGoodNillityWithJackson()
            throws IOException {
        final var name = "good-nillity";
        final var nillity = objectMapper.readValue(
                readXml(name),
                Nillity.class);

        assertThat(nillity.getOuter()).hasSize(1);
        final var outer = nillity.getOuter().get(0);
        assertThat(outer.getUpper().getFoo()).isEqualTo("HI, MOM!");
        assertThat(outer.getInner()).hasSize(2);
    }

    @Test
    void shouldParseGoodNillityWithJaxb()
            throws JAXBException {
        final var nillity = jaxbMapper
                .readValue(readXml("good-nillity"), NillityJaxb.class);

        assertThat(nillity.outer).hasSize(1);
        final var outer = nillity.outer.get(0);
        assertThat(outer.upper.foo).isEqualTo("HI, MOM!");
        assertThat(outer.inner).hasSize(2);
    }

    @Disabled("DEFECT WITH NIL AND LIST")
    @Test
    void shouldParseNilNillityWithJackson()
            throws IOException {
        final var name = "nil-nillity";
        final var nillity = objectMapper.readValue(
                readXml(name),
                Nillity.class);

        assertThat(nillity.getOuter()).hasSize(1);
        final var outer = nillity.getOuter().get(0);
        assertThat(outer.getUpper().getFoo()).isEmpty();
        assertThat(outer.getInner()).hasSize(2);
    }

    @Disabled("DEFECT WITH NIL AND LIST")
    @Test
    void shouldParseNilNillityWithXStream() {
        final var name = "nil-nillity";
        final var xStream = new XStream();
        xStream.alias("nillity", Nillity.class);
        xStream.alias("outer", Outer.class);
        xStream.alias("upper", Upper.class);
        xStream.alias("inner", Inner.class);
        final var nillity = (Nillity) xStream.fromXML(readXml((name)));

        assertThat(nillity.getOuter()).hasSize(1);
        final var outer = nillity.getOuter().get(0);
        assertThat(outer.getUpper().getFoo()).isEmpty();
        assertThat(outer.getInner()).hasSize(2);
    }

    @Test
    void shouldParseNilNillityWithJaxb()
            throws JAXBException {
        final var nillity = jaxbMapper
                .readValue(readXml("nil-nillity"), NillityJaxb.class);

        final NillityJaxb expected = expectedNillityJaxb();

        assertThat(nillity).isEqualTo(expected);
    }

    private static NillityJaxb expectedNillityJaxb() {
        final var expected = new NillityJaxb();
        final var outers = new ArrayList<OuterJaxb>();
        expected.outer = outers;
        final var outer = new OuterJaxb();
        outers.add(outer);
        final var upper = new OuterJaxb.Upper();
        outer.upper = upper;
        upper.foo = "";
        upper.bar = 1;
        final var inners = new ArrayList<OuterJaxb.Inner>();
        outer.inner = inners;
        final var innerA = new OuterJaxb.Inner();
        inners.add(innerA);
        innerA.foo = "QUX";
        innerA.quux = 2;
        final var innerB = new OuterJaxb.Inner();
        inners.add(innerB);
        innerB.foo = "BAR";
        innerB.quux = 3;
        return expected;
    }

    @Test
    void shouldParseOuterWithNillMember()
            throws IOException {
        final var name = "nil-outer";
        final var outer = objectMapper.readValue(
                readXml(name),
                Outer.class);

        assertThat(outer.getUpper().getFoo()).isEmpty();
        assertThat(outer.getInner()).hasSize(2);
    }
}
