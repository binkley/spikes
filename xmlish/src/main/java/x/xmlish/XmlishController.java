package x.xmlish;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class XmlishController {
    private final Logger logger;

    @GetMapping
    public Xmlish get() {
        return new Xmlish("HI, MOM!", 22, Instant.now());
    }

    @PostMapping
    public void post(@RequestBody final Xmlish request) {
        logger.warn("GOT {}", request);
    }
}
