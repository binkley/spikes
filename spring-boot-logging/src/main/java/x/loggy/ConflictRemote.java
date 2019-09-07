package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import static x.loggy.AlertMessage.Severity.MEDIUM;

@FeignClient(name = "conflict",
        url = "http://localhost:8080/_feign/conflict")
public interface ConflictRemote {
    String METRIC_NAME = "conflict.remote";

    @AlertMessage(message = "CONFLICTED", severity = MEDIUM)
    @PostMapping
    @Timed(METRIC_NAME)
    void postConflict();
}
