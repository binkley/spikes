package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import static x.loggy.AlertMessage.Severity.MEDIUM;

@FeignClient(name = "retry",
        url = "http://localhost:8080/feign/retry")
public interface RetryRemote {
    @AlertMessage(message = "RETRYING", severity = MEDIUM)
    @GetMapping
    void getRetry();
}
