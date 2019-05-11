package x.loggy;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import brave.propagation.TraceContextOrSamplingFlags;
import feign.RequestInterceptor;
import feign.RequestTemplate;
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

    @Autowired
    public TraceRequestInterceptor(final Tracing tracing,
            final Tracer tracer) {
        extractor = tracing.propagation().extractor(
                (mdc, key) -> MDC.get(key));
        injector = tracing.propagation().injector(
                RequestTemplate::header);
        this.tracer = tracer;
    }

    @Override
    public void apply(final RequestTemplate template) {
        // TODO: Is there a way to get at the Feign "Class#method" string?
        template.header("User-Agent", getClass().getName());

        final var compoundContext = compoundContext(
                currentContext(tracer),
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

    private static TraceContext currentContext(final Tracer tracer) {
        final var currentSpan = tracer.currentSpan();
        return null == currentSpan
                ? tracer.newTrace().context()
                : currentSpan.context();
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
