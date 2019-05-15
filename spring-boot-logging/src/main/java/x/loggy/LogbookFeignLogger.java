package x.loggy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;

import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static feign.Util.toByteArray;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.http.HttpVersion.HTTP_1_1;

@RequiredArgsConstructor(access = PROTECTED)
public abstract class LogbookFeignLogger
        extends feign.Logger {
    private static final ThreadLocal<HttpContext> threadLocal
            = new ThreadLocal<>();

    protected final Logbook logbook;
    protected final Logger logger;

    @Override
    protected void log(final String configKey,
            final String format, final Object... args) {
        // Do nothing -- Feign logger is unfortunate
    }

    @Override
    protected void logRequest(final String configKey,
            final Level logLevel, final Request feignRequest) {
        try {
            final HttpEntityEnclosingRequest logbookRequest
                    = logbookRequestFor(feignRequest);

            final var context = new HttpClientContext();
            new LogbookHttpRequestInterceptor(logbook).process(
                    logbookRequest, context);
            threadLocal.set(context);
        } catch (final HttpException | IOException e) {
            // It is unfortunate the logbook request interceptor throws
            throw new IOError(e);
        }
    }

    private static HttpEntityEnclosingRequest logbookRequestFor(
            final Request feignRequest) {
        final var logbookRequest
                = new BasicHttpEntityEnclosingRequest(
                feignRequest.httpMethod().name(),
                feignRequest.url(),
                HTTP_1_1);

        copyHeadersTo(feignRequest.headers(), logbookRequest);
        copyBodyTo(bodyData(feignRequest), logbookRequest);

        return logbookRequest;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private static void copyHeadersTo(
            final Map<String, Collection<String>> headers,
            final HttpMessage message) {
        // If "values" is null, consider that a pathological case
        headers.forEach((header, values) ->
                values.forEach(value ->
                        message.addHeader(header, value)));
    }

    private static void copyBodyTo(final byte[] bodyData,
            final HttpEntityEnclosingRequest logbookRequest) {
        logbookRequest.setEntity(new ByteArrayEntity(
                null == bodyData ? new byte[0] : bodyData));
    }

    private static byte[] bodyData(final Request request) {
        final var requestBody = request.requestBody();
        return null == requestBody
                ? new byte[0] : requestBody.asBytes();
    }

    @Override
    protected abstract void logRetry(
            final String configKey, final Level logLevel);

    @Override
    protected Response logAndRebufferResponse(final String configKey,
            final Level logLevel,
            final Response feignResponse,
            final long elapsedTime)
            throws IOException {
        final byte[] bodyData = bodyData(feignResponse);
        final HttpResponse logbookResponse = logbookResponseFor(
                feignResponse, bodyData);

        new LogbookHttpResponseInterceptor().process(
                logbookResponse, threadLocal.get());
        threadLocal.remove();

        return feignResponse.toBuilder().body(bodyData).build();
    }

    private static byte[] bodyData(final Response response)
            throws IOException {
        final var status = response.status();
        if (response.body() != null
                && !(status == 204 || status == 205)) {
            return toByteArray(response.body().asInputStream());
        } else {
            return new byte[0];
        }
    }

    private static HttpResponse logbookResponseFor(
            final Response feignResponse,
            final byte[] bodyData) {
        final var logbookResponse = new BasicHttpResponse(HTTP_1_1,
                feignResponse.status(), feignResponse.reason());

        copyHeadersTo(feignResponse.headers(), logbookResponse);
        copyBodyTo(bodyData, logbookResponse);

        return logbookResponse;
    }

    private static void copyBodyTo(final byte[] bodyData,
            final HttpResponse logbookResponse) {
        logbookResponse.setEntity(new ByteArrayEntity(bodyData));
    }

    /**
     * @todo Move to Logbook 2.x, which *will* log the request, even when
     * there is no response
     */
    @Override
    protected IOException logIOException(final String configKey,
            final Level logLevel, final IOException ioe,
            final long elapsedTime) {
        threadLocal.remove();
        return ioe;
    }

    @Component
    @Profile("!json")
    public static class ConsoleLogbookFeignLogger
            extends LogbookFeignLogger {
        @Autowired
        public ConsoleLogbookFeignLogger(final Logbook logbook,
                final Logger logger) {
            super(logbook, logger);
        }

        @Override
        protected void logRetry(final String configKey,
                final Level logLevel) {
            logger.warn("Retrying {}", configKey);
        }

        @Override
        protected IOException logIOException(final String configKey,
                final Level logLevel, final IOException ioe,
                final long elapsedTime) {
            logger.error("Failed {} after {} ms: {}", configKey,
                    elapsedTime, ioe.toString(), ioe);
            return super.logIOException(
                    configKey, logLevel, ioe, elapsedTime);
        }
    }

    @Component
    @Profile("json")
    public static class JsonLogbookFeignLogger
            extends LogbookFeignLogger {
        private final ObjectMapper objectMapper;

        @Autowired
        public JsonLogbookFeignLogger(final Logbook logbook,
                final Logger logger,
                final ObjectMapper objectMapper) {
            super(logbook, logger);
            this.objectMapper = objectMapper;
        }

        @Override
        protected void logRetry(final String configKey,
                final Level logLevel) {
            try {
                logger.warn(objectMapper.writeValueAsString(
                        new Retrying(configKey)));
            } catch (final JsonProcessingException e) {
                throw new Bug("Jackson missing or misconfigured", e);
            }
        }

        @Override
        protected IOException logIOException(final String configKey,
                final Level logLevel, final IOException ioe,
                final long elapsedTime) {
            try {
                logger.error(objectMapper.writeValueAsString(
                        new IoException(configKey, elapsedTime,
                                ioe.toString())),
                        ioe);
            } catch (final JsonProcessingException e) {
                throw new Bug("Jackson missing or misconfigured", e);
            }
            return super.logIOException(
                    configKey, logLevel, ioe, elapsedTime);
        }

        @Value
        private static final class Retrying {
            String message = "retrying";
            String configKey;
        }

        @Value
        private static final class IoException {
            String message = "ioException";
            String configKey;
            long elapsedTime;
            String exception;
        }
    }
}
