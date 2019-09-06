package x.loggy.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import x.loggy.LoggyProperties;

@Configuration
@EnableConfigurationProperties(LoggyProperties.class)
public class PropertiesConfiguration {
}
