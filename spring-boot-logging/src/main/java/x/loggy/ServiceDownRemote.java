package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import static x.loggy.AlertMessage.Severity.HIGH;

@FeignClient(name = "service-down", url = "https://localhost:0")
public interface ServiceDownRemote {
    @AlertMessage(message = "SERVICE DOWN", severity = HIGH)
    @GetMapping(produces = "application/json")
    @Timed("service-down.remote")
    String get();
}
