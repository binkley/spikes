package x.loggy;

import feign.codec.ErrorDecoder;
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

    @Bean
    public ErrorDecoder errorDecoder() {
        final var delegate = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            final var exception = delegate.decode(methodKey, response);
            if (null != exception)
                exception.addSuppressed(new FeignErrorDetails(
                        response.request().httpMethod(),
                        response.request().url()));
            return exception;
        };
    }
}
