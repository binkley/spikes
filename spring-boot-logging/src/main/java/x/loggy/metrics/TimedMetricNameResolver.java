package x.loggy.metrics;

import org.aopalliance.intercept.MethodInvocation;

import java.util.function.BiFunction;

public interface TimedMetricNameResolver
        extends BiFunction<String, MethodInvocation, String> {
}
