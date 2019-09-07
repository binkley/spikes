package x.loggy.configuration;

import feign.Retryer;
import feign.Retryer.Default;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import x.loggy.LogbookFeignLogger;
import x.loggy.LoggyErrorDecoder;

@Configuration
public class FeignConfiguration {
    @Bean
    public Retryer defaultRetryer() {
        return new Default(100L, 1000L, 2);
    }

    @Bean
    public FeignLoggerFactory replaceFeignLoggerWithLogbook(
            final LogbookFeignLogger logger) {
        return type -> logger;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new LoggyErrorDecoder();
    }
}
