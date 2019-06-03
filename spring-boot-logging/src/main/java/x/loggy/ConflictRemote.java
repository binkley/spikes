package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "conflict",
        url = "http://localhost:8080/feign/conflict")
public interface ConflictRemote {
    @AlertMessage(message = "CONFLICTED")
    @PostMapping
    void postConflict();
}
