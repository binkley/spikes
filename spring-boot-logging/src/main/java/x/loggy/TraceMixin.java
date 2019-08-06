package x.loggy;

import brave.Tracer;
import brave.propagation.TraceContext;

public interface TraceMixin {
    default TraceContext currentContext(final Tracer tracer) {
        var currentSpan = tracer.currentSpan();
        if (null == currentSpan) {
            currentSpan = tracer.newTrace();
        }
        return currentSpan.context();
    }
}
