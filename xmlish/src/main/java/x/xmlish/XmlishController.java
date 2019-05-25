package x.xmlish;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import x.xmlish.Xmlish.Inner;

import javax.validation.Valid;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.SECONDS;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class XmlishController {
    private final Logger logger;

    @GetMapping
    public Xmlish get() {
        return Xmlish.builder()
                .foo("HI, MOM!")
                .barNone(22)
                .when(Instant.now())
                .inner(Inner.builder()
                        .qux("BYE, DAD!")
                        .quux(77)
                        .ever(Instant.now().minus(1_000_000L, SECONDS))
                        .build())
                .build();
    }

    @PostMapping
    public void post(@RequestBody final Xmlish request) {
        logger.warn("GOT {}", request);
    }

    @PostMapping("complex")
    public void postComplex(
            @RequestBody @Valid final ComplexExample request) {
        logger.warn("GOT {}", request);
    }
}
