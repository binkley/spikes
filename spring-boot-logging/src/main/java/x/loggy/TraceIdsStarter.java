package x.loggy;

import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import org.slf4j.spi.MDCAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static brave.propagation.TraceContextOrSamplingFlags.EMPTY;
import static org.slf4j.MDC.getMDCAdapter;

@Component
public class TraceIdsStarter {
    private final Tracing tracing;
    private final Extractor<MDCAdapter> extractor;
    private final Injector<MDCAdapter> injector;

    @Autowired
    public TraceIdsStarter(final Tracing tracing) {
        this.tracing = tracing;
        final var propagation = tracing.propagation();
        extractor = propagation.extractor(MDCAdapter::get);
        injector = propagation.injector(MDCAdapter::put);
    }

    public TraceContext newTraceIdsOnCurrentThread() {
        final var mdc = getMDCAdapter();
        final var extract = extractor.extract(mdc);

        if (extract != EMPTY) {
            // TODO: Bug, or only add new if trace IDs not already present?
            throw new Bug(
                    "Starting new trace IDs when there are existing ones: "
                            + extract);
        }

        // getMDCAdapter() is per-thread, not scope or bean
        final var context = tracing.tracer().newTrace().context();
        injector.inject(context, mdc);
        return context;
    }
}
