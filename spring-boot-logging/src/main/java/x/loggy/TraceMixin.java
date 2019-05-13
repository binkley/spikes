package x.loggy;

import brave.Tracer;
import brave.propagation.TraceContext;
import org.slf4j.Logger;

public interface TraceMixin {
    default TraceContext currentContext(
            final Tracer tracer, final Logger logger) {
        var currentSpan = tracer.currentSpan();
        if (null == currentSpan) {
            currentSpan = tracer.newTrace();
            logger.trace("No current tracing span; created: {}", currentSpan);
        } else {
            logger.trace("Current tracing span: {}", currentSpan);
        }
        return currentSpan.context();
    }
}
