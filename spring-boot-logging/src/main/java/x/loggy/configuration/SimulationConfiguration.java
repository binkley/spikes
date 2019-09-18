package x.loggy.configuration;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import x.loggy.LoggyProperties;
import x.loggy.SimulationResponseFilter;

@Configuration
public class SimulationConfiguration {
    @Bean
    @ConditionalOnProperty(value = "loggy.simulate-slow-responses")
    public SimulationResponseFilter simulateSlowResponsesResponseFilter(
            final LoggyProperties properties, final Logger logger) {
        return new SimulationResponseFilter(
                properties.getSimulateSlowResponses(), logger);
    }
}
