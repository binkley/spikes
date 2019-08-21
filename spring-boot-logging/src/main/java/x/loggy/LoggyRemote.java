package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "loggy", url = "http://localhost:8080")
public interface LoggyRemote {
    @GetMapping(path = "direct", produces = "application/json")
    @Timed("bob.uncle")
    LoggyResponse getDirect();

    @GetMapping(path = "indirect", produces = "application/json")
    LoggyResponse getIndirect();

    @PostMapping(path = "postish")
    void post(final LoggyRequest request);

    @GetMapping(path = "npe")
    LoggyResponse getNpe();

    @PostMapping(path = "conflict")
    void postConflict();

    @GetMapping(path = "ping")
    void getPing();
}
