package x.etaggy;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EtaggyController {
    @GetMapping
    public EtaggyResponse get() {
        return new EtaggyResponse("HI, MOM!", 22, Instant.now());
    }

    @Value
    static class EtaggyResponse {
        String foo;
        int barNone;
        Instant when;
    }
}
