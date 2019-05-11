package x.loggy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loggy")
@Data
public class LoggyProperties {
    private boolean enabled;
}
