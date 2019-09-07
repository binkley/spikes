package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import static x.loggy.AlertMessage.Severity.MEDIUM;

@FeignClient(name = "retry",
        url = "http://localhost:8080/_feign/retry")
public interface RetryRemote {
    String METRIC_NAME = "retry.remote";

    @AlertMessage(message = "RETRYING", severity = MEDIUM)
    @GetMapping
    @Timed(METRIC_NAME)
    void getRetry();
}
