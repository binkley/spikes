package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "loggy", url = "http://localhost:8080")
public interface LoggyRemote {
    @GetMapping(produces = "application/json")
    LoggyResponse get();
}
