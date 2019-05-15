package x.loggy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "sad-path", url = "https://not.really.a.place/get")
public interface UnknownHostRemote {
    @GetMapping(produces = "application/json")
    String get();
}
