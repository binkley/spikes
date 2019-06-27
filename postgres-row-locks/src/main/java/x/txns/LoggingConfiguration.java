package x.txns;

import org.slf4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Configuration
public class LoggingConfiguration {
    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public Logger logger(final InjectionPoint at) {
        return getLogger(requireNonNull(at.getMethodParameter())
                .getContainingClass());
    }
}
