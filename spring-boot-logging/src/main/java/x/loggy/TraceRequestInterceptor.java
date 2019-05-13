package x.loggy;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.spi.MDCAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static brave.propagation.TraceContextOrSamplingFlags.EMPTY;
import static org.slf4j.MDC.getMDCAdapter;

@Component
public class TraceRequestInterceptor
        implements RequestInterceptor, TraceMixin {
    private final Tracer tracer;
    private final Extractor<MDCAdapter> extractor;
    private final Injector<RequestTemplate> injector;
    private final Logger logger;

    @Autowired
    public TraceRequestInterceptor(final Tracing tracing,
            final Tracer tracer, final Logger logger) {
        extractor = tracing.propagation().extractor(MDCAdapter::get);
        injector = tracing.propagation().injector(RequestTemplate::header);
        this.tracer = tracer;
        this.logger = logger;
    }

    @Override
    public void apply(final RequestTemplate template) {
        // TODO: Is there a way to get at the Feign "Class#method" string?
        template.header("User-Agent", getClass().getName());

        // With better library support, `currentContext()` should do this
        // for us, which is what happens with HTTP controllers, but with
        // Feign we need to check the MDC ourselves
        final var extraction = extractor.extract(getMDCAdapter());
        final var currentContext = EMPTY == extraction ?
                currentContext(tracer, logger) : extraction.context();

        injector.inject(currentContext, template);
    }
}
