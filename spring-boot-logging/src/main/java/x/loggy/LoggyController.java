package x.loggy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggyController {
    private final Clock clock;
    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;
    private final Logger logger;

    @GetMapping("direct")
    public LoggyResponse getDirect() {
        logger.info("INTER THE DIRECT WEBS");
        return new LoggyResponse("HI, MOM!", 22, Instant.now(clock));
    }

    @GetMapping("indirect")
    public LoggyResponse getIndirect() {
        logger.info("INTER THE FEIGNEY WEBS");
        return loggy.getDirect();
    }

    @GetMapping("not-found")
    public String getNotFound() {
        logger.info("INTER THE NOT-FOUND WEBS");
        return notFound.get();
    }
}
