package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "not-found",
        url = "http://localhost:8080/feign/not-found")
public interface NotFoundRemote {
    @GetMapping(produces = "application/json")
    String get();
}
