package x.loggy;

import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

import static java.lang.String.format;
import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class ExceptionHandling
        implements ProblemHandling, SecurityAdviceTrait {
    @Override
    public StatusType defaultConstraintViolationStatus() {
        return UNPROCESSABLE_ENTITY;
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Problem> handleFeign(
            final FeignException e, final NativeWebRequest request) {
        final var problem = Problem.builder()
                .withDetail(format("%s: %s", e, getMostSpecificCause(e)))
                .withStatus(BAD_GATEWAY)
                .build();
        return create(problem, request);
    }
}
