package x.loggy;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients
public class FeignConfiguration {
    @Bean
    public FeignLoggerFactory replaceFeignLoggerWithLogbook(
            final LogbookFeignLogger logger) {
        return type -> logger;
    }
}
