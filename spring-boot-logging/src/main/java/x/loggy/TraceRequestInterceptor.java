package x.loggy;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import brave.propagation.TraceContextOrSamplingFlags;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.slf4j.MDC.getMDCAdapter;

@Component
public class TraceRequestInterceptor
        implements RequestInterceptor {
    private final Tracer tracer;
    private final Extractor<MDCAdapter> extractor;
    private final Injector<RequestTemplate> injector;
    private final Logger logger;

    @Autowired
    public TraceRequestInterceptor(final Tracing tracing,
            final Tracer tracer, final Logger logger) {
        extractor = tracing.propagation().extractor(
                (mdc, key) -> MDC.get(key));
        injector = tracing.propagation().injector(
                RequestTemplate::header);
        this.tracer = tracer;
        this.logger = logger;
    }

    @Override
    public void apply(final RequestTemplate template) {
        // TODO: Is there a way to get at the Feign "Class#method" string?
        template.header("User-Agent", getClass().getName());

        final var compoundContext = compoundContext(
                currentContext(),
                extractor.extract(getMDCAdapter()));

        injector.inject(compoundContext, template);
    }

    private static TraceContext compoundContext(
            final TraceContext currentContext,
            final TraceContextOrSamplingFlags extraction) {
        return TraceContext.newBuilder()
                .debug(currentContext.debug())
                .parentId(currentContext.parentId())
                .sampled(currentContext.sampled())
                .spanId(currentContext.spanId())
                .traceId(workingTraceId(extraction, currentContext))
                .build();
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

    private static long workingTraceId(
            final TraceContextOrSamplingFlags extraction,
            final TraceContext currentContext) {
        final TraceContext requestContext = extraction.context();
        return null == requestContext
                ? currentContext.traceId()
                : requestContext.traceId();
    }
}
