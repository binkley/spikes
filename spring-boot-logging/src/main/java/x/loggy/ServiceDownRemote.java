package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import static x.loggy.AlertMessage.Severity.HIGH;

// TODO: This will be confusing when something IS running on 17171
@FeignClient(name = "service-down", url = "https://localhost:17171")
public interface ServiceDownRemote {
    String METRIC_NAME = "service-down.remote";

    @AlertMessage(message = "SERVICE DOWN", severity = HIGH)
    @GetMapping(produces = "application/json")
    @Timed(METRIC_NAME)
    String get();
}
