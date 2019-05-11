package x.loggy;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import org.slf4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TraceResponseFilter
        extends OncePerRequestFilter {
    private final Tracer tracer;
    private final Extractor<HttpServletRequest> extractor;
    private final Injector<HttpServletResponse> injector;
    private final Logger logger;

    public TraceResponseFilter(final Tracing tracing, final Tracer tracer,
            final Logger logger) {
        extractor = tracing.propagation().extractor(
                HttpServletRequest::getHeader);
        injector = tracing.propagation().injector(
                HttpServletResponse::setHeader);
        this.tracer = tracer;
        this.logger = logger;
    }

    @Override
    public void doFilterInternal(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        // First, so Spring Cloud bits can setup Sleuth
        chain.doFilter(request, response);

        injector.inject(currentContext(), response);
    }

    private TraceContext currentContext() {
        var currentSpan = tracer.currentSpan();
        if (null != currentSpan) {
            logger.trace("Current tracing span: {}", currentSpan);
            return currentSpan.context();
        }
        currentSpan = tracer.newTrace();
        logger.trace("No current span; created: {}", currentSpan);
        return currentSpan.context();
    }
}
