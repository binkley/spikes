package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "not-found",
        url = "http://localhost:8080/feign/not-found")
public interface NotFoundRemote {
    String METRIC_NAME = "not-found.remote";

    @GetMapping(produces = "application/json")
    @Timed(METRIC_NAME)
    String get();
}
