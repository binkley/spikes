package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "loggy", url = "http://localhost:8080")
public interface LoggyRemote {
    String METRIC_NAME = "loggy.remote";

    @GetMapping(path = "direct", produces = "application/json")
    @Timed(METRIC_NAME)
    LoggyResponse getDirect();

    @GetMapping(path = "indirect", produces = "application/json")
    @Timed(METRIC_NAME)
    LoggyResponse getIndirect();

    @PostMapping(path = "postish")
    @Timed(METRIC_NAME)
    void post(final LoggyRequest request);

    @GetMapping(path = "npe")
    @Timed(METRIC_NAME)
    LoggyResponse getNpe();

    @PostMapping(path = "conflict")
    @Timed(METRIC_NAME)
    void postConflict();

    @GetMapping(path = "ping")
    @Timed(METRIC_NAME)
    void getPing();
}
