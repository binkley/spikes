package x.loggy.ext;

import io.micrometer.core.annotation.Timed;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class TimedAnnotationAdvisor
        extends AbstractPointcutAdvisor {

    private final Pointcut pointcut;
    private final MethodInterceptor advice;

    public TimedAnnotationAdvisor(
            final TimedMethodInterceptor timedMethodInterceptor) {
        final Pointcut cpc = new AnnotationMatchingPointcut(Timed.class,
                true);
        final Pointcut mpc = new ComposablePointcut(
                new InheritedAnnotationMethodMatcher(Timed.class));
        pointcut = new ComposablePointcut(cpc).union(mpc);
        advice = timedMethodInterceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    private static class InheritedAnnotationMethodMatcher
            extends AnnotationMethodMatcher {

        private final Class<? extends Annotation> annotationType;

        public InheritedAnnotationMethodMatcher(
                final Class<? extends Annotation> annotationType) {
            super(annotationType);
            this.annotationType = annotationType;
        }

        @Override
        public boolean matches(final Method method,
                final Class<?> targetClass) {
            return AnnotationUtils.findAnnotation(method, annotationType)
                    != null || super.matches(method, targetClass);
        }
    }
}
