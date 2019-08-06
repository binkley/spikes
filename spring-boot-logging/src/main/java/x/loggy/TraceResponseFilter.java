package x.loggy;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Injector;
import org.slf4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TraceResponseFilter
        extends OncePerRequestFilter
        implements TraceMixin {
    private final Tracer tracer;
    private final Injector<HttpServletResponse> injector;
    private final Logger logger;

    public TraceResponseFilter(final Tracing tracing, final Tracer tracer,
            final Logger logger) {
        final var propagation = tracing.propagation();
        injector = propagation.injector(HttpServletResponse::setHeader);
        this.tracer = tracer;
        this.logger = logger;
    }

    @Override
    public void doFilterInternal(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final var currentContext = currentContext(tracer);

        warnIfRequestTraceHeadersInvalid(request, currentContext);

        injector.inject(currentContext, response);

        chain.doFilter(request, response);
    }

    private void warnIfRequestTraceHeadersInvalid(
            final HttpServletRequest request,
            final TraceContext currentContext) {
        final var requestTraceId = request.getHeader("X-B3-TraceId");
        final var traceId = currentContext.traceIdString();
        final var requestSpanId = request.getHeader("X-B3-SpanId");
        final var spanId = currentContext.spanIdString();

        // Strictly, some shorter strings are accepted, and zero-padded;
        // however enforcing the strict rules is unwieldy, and the simple
        // rule below is easy to understand and follow.
        if ((null != requestTraceId && !requestTraceId.equals(traceId))
                || (null != requestSpanId && !requestSpanId.equals(spanId)))
            logger.warn(
                    "Invalid X-B3-TraceId or X-B3-SpanId: {}/{}: both must be"
                            + " 16-digit hexadecimal strings, all lower-case,"
                            + " and are required; ignoring and generating"
                            + " new trace/span IDs: {}/{}",
                    requestTraceId, requestSpanId, traceId, spanId);
    }
}
