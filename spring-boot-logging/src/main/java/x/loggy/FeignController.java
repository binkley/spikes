package x.loggy;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@RequestMapping("feign")
public class FeignController {
    @PostMapping("conflict")
    @ResponseStatus(CONFLICT)
    public void conflict() {
    }
}
