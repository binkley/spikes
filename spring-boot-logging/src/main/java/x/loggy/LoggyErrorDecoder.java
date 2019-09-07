package x.loggy;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

public final class LoggyErrorDecoder
        extends ErrorDecoder.Default {
    @Override
    public Exception decode(final String methodKey,
            final Response response) {
        final var exception = super.decode(methodKey, response);
        if (null != exception)
            exception.addSuppressed(new FeignErrorDetails(
                    response.request().httpMethod(),
                    response.request().url()));
        if (500 <= response.status() && response.status() < 600)
            return new RetryableException(
                    response.status(),
                    null == exception ? null : exception.getMessage(),
                    response.request().httpMethod(), exception, null);
        return exception;
    }
}
