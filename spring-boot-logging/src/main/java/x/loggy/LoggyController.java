package x.loggy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.nio.charset.CoderMalfunctionError;
import java.time.Clock;
import java.time.Instant;

import static org.springframework.http.HttpStatus.ACCEPTED;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggyController {
    private final Clock clock;
    private final LoggyRemote loggy;
    private final NotFoundRemote notFound;
    private final UnknownHostRemote unknownHost;
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

    @PostMapping("postish")
    @ResponseStatus(ACCEPTED)
    public void post(@RequestBody final @Valid LoggyRequest request) {
        logger.info("POSTED {}", request);
    }

    @GetMapping("npe")
    public LoggyResponse getNpe() {
        logger.info("INTER THE SAD WEBS");
        throw new CoderMalfunctionError(new NullPointerException("SAD, SAD"));
    }

    @GetMapping("not-found")
    public String getNotFound() {
        logger.info("INTER THE NOT-FOUND WEBS");
        return notFound.get();
    }

    @GetMapping("unknown-host")
    public String getUnknownHost() {
        logger.info("INTER THE UNKNOWN_HOST WEBS");
        return unknownHost.get();
    }
}
