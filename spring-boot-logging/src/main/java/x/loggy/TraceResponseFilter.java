package x.loggy;

import brave.Tracer;
import brave.Tracing;
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
        injector.inject(currentContext(tracer, logger), response);

        chain.doFilter(request, response);
    }
}
