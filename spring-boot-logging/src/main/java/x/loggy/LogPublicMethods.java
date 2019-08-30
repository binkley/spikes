package x.loggy;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;

@Aspect
@Component
@Slf4j
public class LogPublicMethods {
    @Around("execution(public * x.loggy.*Controller.*(..))")
    public Object logPublicMethods(final ProceedingJoinPoint pjp)
            throws Throwable {
        final MethodSignature method = (MethodSignature) pjp.getSignature();
        final String className = method.getDeclaringType().getSimpleName();
        final String methodName = method.getName();

        try {
            final Object result = pjp.proceed();
            log.trace("NORMAL {}.{}", className, methodName);
            return result;
        } catch (final Throwable throwable) {
            final var rootCause = getMostSpecificCause(throwable);
            log.warn("FAILED {}.{}: {} at {}", className, methodName,
                    rootCause, nearestUs(rootCause));
            throw throwable;
        }
    }

    private static String nearestUs(final Throwable t) {
        for (final var frame : t.getStackTrace()) {
            if (frame.getClassName().startsWith("x.loggy."))
                return frame.toString();
        }
        return "JDK INTERNAL";
    }
}
