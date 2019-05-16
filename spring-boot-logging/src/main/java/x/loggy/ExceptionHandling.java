package x.loggy;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

import javax.servlet.http.HttpServletRequest;

import static java.lang.String.format;
import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.UNPROCESSABLE_ENTITY;

@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExceptionHandling
        implements ProblemHandling, SecurityAdviceTrait {
    private final Logger logger;

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

    @Override
    public void log(
            @NonNull final Throwable throwable,
            final Problem problem,
            final NativeWebRequest request,
            final HttpStatus status) {
        final var realRequest = request
                .getNativeRequest(HttpServletRequest.class);
        final var requestURL = realRequest.getRequestURL();

        if (status.is4xxClientError()) {
            logger.warn("{}: {}: {}",
                    status.getReasonPhrase(),
                    requestURL,
                    throwable.getMessage());
        } else if (status.is5xxServerError()) {
            logger.error("{}: {}",
                    status.getReasonPhrase(), requestURL, throwable);
        }
    }
}
