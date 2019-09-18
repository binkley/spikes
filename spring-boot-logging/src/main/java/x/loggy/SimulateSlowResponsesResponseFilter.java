package x.loggy;

import org.slf4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SimulateSlowResponsesResponseFilter
        extends OncePerRequestFilter
        implements TraceMixin {
    private final Duration delay;
    private final Logger logger;

    public SimulateSlowResponsesResponseFilter(
            final Duration delay,
            final Logger logger) {
        this.delay = delay;
        this.logger = logger;
    }

    @Override
    public void doFilterInternal(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        logger.info("Simulating slow response of {} to {}",
                delay, request.getRequestURL());

        try {
            sleep(MILLISECONDS.convert(delay.getSeconds(), SECONDS),
                    delay.getNano());
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }

        chain.doFilter(request, response);
    }
}
