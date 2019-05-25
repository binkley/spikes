package x.xmlish;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

import static x.xmlish.ReadXml.readXml;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class XmlishController {
    private final Logger logger;
    private final ObjectMapper objectMapper;

    @GetMapping
    public Xmlish get()
            throws IOException {
        return objectMapper.readValue(readXml("good-xmlish"), Xmlish.class);
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
