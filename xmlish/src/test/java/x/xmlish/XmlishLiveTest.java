package x.xmlish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import x.xmlish.OuterJackson.Inner;
import x.xmlish.OuterJackson.Upper;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static x.xmlish.ReadXml.readXml;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = NONE)
@TestInstance(PER_CLASS)
class XmlishLiveTest {
    private static final Instant aInstant = Instant.ofEpochSecond(
            123L, 456_000_000L);
    private static final Instant bInstant = Instant.ofEpochSecond(
            456_000_000L, 789_000_000L);
    private static final LocalDate aLocalDate = LocalDate.of(2011, 2, 3);
    private static final LocalDate bLocalDate = LocalDate.of(1999, 12, 13);

    private final ObjectMapper objectMapper;
    private final JaxbMapper jaxbMapper = new JaxbMapper();

    @Test
    void shouldParseGoodNillityWithJackson()
            throws IOException {
        final var name = "good-nillity";
        final var nillity = objectMapper
                .readValue(readXml(name), NillityJackson.class);

        assertThat(nillity).isEqualTo(expectedGoodNillityJackson());
    }

    private static NillityJackson expectedGoodNillityJackson() {
        return new NillityJackson(
                List.of(new OuterJackson(new Upper("HI, MOM!", 1), List.of(
                        new Inner("QUX", 2, aInstant, aLocalDate),
                        new Inner("BAR", 3, bInstant, bLocalDate)))));
    }

    @Test
    void shouldParseGoodNillityWithJaxb()
            throws JAXBException {
        final var nillity = jaxbMapper
                .readValue(readXml("good-nillity"), NillityJaxb.class);

        assertThat(nillity).isEqualTo(expectedGoodNillityJaxb());
    }

    private static NillityJaxb expectedGoodNillityJaxb() {
        final var expected = new NillityJaxb();
        expected.outer = new ArrayList<>();
        final var outer = new OuterJaxb();
        expected.outer.add(outer);
        final var upper = new OuterJaxb.Upper();
        outer.upper = upper;
        upper.foo = "HI, MOM!";
        upper.bar = 1;
        outer.inner = new ArrayList<>();
        final var innerA = new OuterJaxb.Inner();
        outer.inner.add(innerA);
        innerA.foo = "QUX";
        innerA.quux = 2;
        innerA.when = aInstant;
        innerA.day = aLocalDate;
        final var innerB = new OuterJaxb.Inner();
        outer.inner.add(innerB);
        innerB.foo = "BAR";
        innerB.quux = 3;
        innerB.when = bInstant;
        innerB.day = bLocalDate;
        return expected;
    }

    @Disabled("DEFECT WITH NIL AND LIST")
    @Test
    void shouldParseNilNillityWithJackson()
            throws IOException {
        final var nillity = objectMapper.readValue(
                readXml("nil-nillity"),
                NillityJackson.class);

        assertThat(nillity).isEqualTo(expectedNilNillityJaxb());
    }

    private static NillityJaxb expectedNilNillityJaxb() {
        final var expected = new NillityJaxb();
        expected.outer = new ArrayList<>();
        final var outer = new OuterJaxb();
        expected.outer.add(outer);
        final var upper = new OuterJaxb.Upper();
        outer.upper = upper;
        upper.foo = "";
        upper.bar = 1;
        outer.inner = new ArrayList<>();
        final var innerA = new OuterJaxb.Inner();
        outer.inner.add(innerA);
        innerA.foo = "";
        innerA.quux = 2;
        innerA.when = aInstant;
        innerA.day = aLocalDate;
        final var innerB = new OuterJaxb.Inner();
        outer.inner.add(innerB);
        innerB.foo = "BAR";
        innerB.quux = 3;
        innerB.when = bInstant;
        innerB.day = bLocalDate;
        return expected;
    }

    @Disabled("DEFECT WITH NIL AND LIST")
    @Test
    void shouldParseNilNillityWithXStream() {
        final var xStream = new XStream();
        xStream.alias("nillity", NillityJackson.class);
        xStream.alias("outer", OuterJackson.class);
        xStream.alias("upper", Upper.class);
        xStream.alias("inner", Inner.class);
        final var nillity = (NillityJackson) xStream
                .fromXML(readXml(("nil-nillity")));

        assertThat(nillity).isEqualTo(expectedNilNillityJaxb());
    }

    @Test
    void shouldParseNilNillityWithJaxb()
            throws JAXBException {
        final var nillity = jaxbMapper
                .readValue(readXml("nil-nillity"), NillityJaxb.class);

        assertThat(nillity).isEqualTo(expectedNilNillityJaxb());
    }
}
