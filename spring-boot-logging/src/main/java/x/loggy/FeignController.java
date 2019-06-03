package x.loggy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeignController {
    private final AtomicBoolean flipflop = new AtomicBoolean();

    private final Logger logger;

    @PostMapping("conflict")
    @ResponseStatus(CONFLICT)
    public void conflict() {
        logger.warn("IN FEIGN CONFLICT");
    }

    @GetMapping("retry")
    public ResponseEntity<?> retry() {
        final var flipped = flipflop.get();
        logger.warn("IN FEIGN RETRY: {}", flipped);
        flipflop.set(!flipped);
        return status(flipped ? OK : SERVICE_UNAVAILABLE).build();
    }
}
