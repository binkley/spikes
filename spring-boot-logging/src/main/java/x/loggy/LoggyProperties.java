package x.loggy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("loggy")
@Data
public class LoggyProperties {
    private boolean enableDemo;
    private boolean logFeignRetries;
    private boolean runOnce;
    private Duration simulateSlowResponses;
}
