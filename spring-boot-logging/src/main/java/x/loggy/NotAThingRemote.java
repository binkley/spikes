package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "not-found",
        url = "https://jsonplaceholder.typicode.com/not-found")
public interface NotAThingRemote {
    @GetMapping(produces = "application/json")
    String get();
}