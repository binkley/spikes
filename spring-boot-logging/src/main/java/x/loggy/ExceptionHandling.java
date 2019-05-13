package x.loggy;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.StatusType;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

import static org.zalando.problem.Status.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class ExceptionHandling
        implements ProblemHandling, SecurityAdviceTrait {
    @Override
    public StatusType defaultConstraintViolationStatus() {
        return UNPROCESSABLE_ENTITY;
    }
}
