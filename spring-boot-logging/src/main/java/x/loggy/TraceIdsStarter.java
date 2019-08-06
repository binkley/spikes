package x.loggy;

import brave.Tracing;
import brave.propagation.TraceContext.Injector;
import org.slf4j.spi.MDCAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.slf4j.MDC.getMDCAdapter;

@Component
public class TraceIdsStarter {
    private final Tracing tracing;
    private final Injector<MDCAdapter> injector;

    @Autowired
    public TraceIdsStarter(final Tracing tracing) {
        this.tracing = tracing;
        injector = tracing.propagation().injector(MDCAdapter::put);
    }

    public void newTraceIdsOnCurrentThread() {
        // getMDCAdapter() is per-thread, not scope or bean
        injector.inject(tracing.tracer().newTrace().context(),
                getMDCAdapter());
    }
}
