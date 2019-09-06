package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import static x.loggy.AlertMessage.Severity.HIGH;

@FeignClient(name = "service-down", url = "https://localhost:0")
public interface ServiceDownRemote {
    @AlertMessage(message = "SERVICE DOWN", severity = HIGH)
    @GetMapping(produces = "application/json")
    String get();
}
