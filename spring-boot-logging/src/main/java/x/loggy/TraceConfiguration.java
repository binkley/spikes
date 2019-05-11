package x.loggy;

import brave.Tracer;
import brave.Tracing;
import org.slf4j.Logger;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TraceAutoConfiguration.class)
public class TraceConfiguration {
    @Bean
    public TraceResponseFilter traceResponseFilter(
            final Tracing tracing, final Tracer tracer, final Logger logger) {
        return new TraceResponseFilter(tracing, tracer, logger);
    }
}
