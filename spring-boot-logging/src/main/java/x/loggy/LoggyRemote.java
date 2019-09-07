package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "loggy", url = "http://localhost:8080")
public interface LoggyRemote {
    @GetMapping(path = "direct", produces = "application/json")
    @Timed("loggy.remote")
    LoggyResponse getDirect();

    @GetMapping(path = "indirect", produces = "application/json")
    @Timed("loggy.remote")
    LoggyResponse getIndirect();

    @PostMapping(path = "postish")
    @Timed("loggy.remote")
    void post(final LoggyRequest request);

    @GetMapping(path = "npe")
    @Timed("loggy.remote")
    LoggyResponse getNpe();

    @PostMapping(path = "conflict")
    @Timed("loggy.remote")
    void postConflict();

    @GetMapping(path = "ping")
    @Timed("loggy.remote")
    void getPing();
}
