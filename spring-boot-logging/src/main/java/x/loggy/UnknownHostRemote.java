package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import static x.loggy.AlertMessage.Severity.HIGH;

@FeignClient(name = "sad-path", url = "https://not.really.a.place/get")
public interface UnknownHostRemote {
    @AlertMessage(message = "UNKNOWABLE HOST", severity = HIGH)
    @GetMapping(produces = "application/json")
    String get();
}
