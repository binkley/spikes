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
    private final Logger logger;

    @GetMapping
    public LoggyResponse get() {
        logger.info("INTER THE WEBS");
        return new LoggyResponse("HI, MOM!", 22, Instant.now(clock));
    }
}
