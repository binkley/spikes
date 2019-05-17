package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

@FeignClient(name = "conflict",
        url = "http://localhost:8080/feign/conflict")
public interface ConflictRemote {
    @PostMapping
    @ResponseStatus(CONFLICT)
    void postConflict();
}
