package x.loggy.configuration;

import org.slf4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.ResponseFilter;
import x.loggy.LogbookBodyOnErrorsOnlyFilter;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Configuration
public class LoggingConfiguration {
    private static final LogbookBodyOnErrorsOnlyFilter logbookFilter
            = new LogbookBodyOnErrorsOnlyFilter();

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public Logger logger(final InjectionPoint at) {
        return getLogger(requireNonNull(at.getMethodParameter())
                .getContainingClass());
    }

    @Bean
    public RequestFilter noRequestBodyLoggingFilter() {
        return logbookFilter;
    }

    @Bean
    public ResponseFilter responseBodyLoggingOnErrorsOnlyFilter() {
        return logbookFilter;
    }
}
