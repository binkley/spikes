package x.xmlish;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class XmlishController {
    @GetMapping(produces = APPLICATION_XML_VALUE)
    public XmlishResponse get() {
        return new XmlishResponse("HI, MOM!", 22, Instant.now());
    }
}
