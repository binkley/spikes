package x.loggy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("feign")
public class FeignController {
    private final AtomicBoolean flipflop = new AtomicBoolean();

    @PostMapping("conflict")
    @ResponseStatus(CONFLICT)
    public void conflict() {
    }

    @GetMapping("retry")
    public ResponseEntity<?> retry() {
        final var flipped = flipflop.get();
        flipflop.set(!flipped);
        return status(flipped ? OK : SERVICE_UNAVAILABLE).build();
    }
}
