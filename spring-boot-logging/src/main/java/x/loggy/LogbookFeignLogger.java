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
import static java.lang.String.format;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.http.HttpVersion.HTTP_1_1;

@RequiredArgsConstructor(access = PROTECTED)
public abstract class LogbookFeignLogger
        extends feign.Logger {
    private static final ThreadLocal<HttpContext> threadLocal
            = new ThreadLocal<>();

    protected final Logbook logbook;
    protected final Logger logger;
    protected final LoggyProperties loggy;

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

    @Override
    protected abstract void log(final String configKey,
            final String format, final Object... args);

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

    /**
     * @todo Move to Logbook 2.x, which *will* log the request, even when
     * there is no response
     */
    @Override
    protected IOException logIOException(
            final String configKey,
            final Level logLevel,
            final IOException ioe,
            final long elapsedTime) {
        threadLocal.remove();
        return ioe;
    }

    @Component
    @Profile("!json")
    public static class ConsoleLogbookFeignLogger
            extends LogbookFeignLogger {
        @Autowired
        public ConsoleLogbookFeignLogger(
                final Logbook logbook,
                final Logger logger,
                final LoggyProperties loggy) {
            super(logbook, logger, loggy);
        }

        @Override
        protected void log(final String configKey, final String format,
                final Object... args) {
            logger.trace("Logging {}: {}", configKey, format(format, args));
        }

        @Override
        protected void logRetry(
                final String configKey, final Level logLevel) {
            if (loggy.isLogFeignRetries())
                logger.warn("Retrying {}", configKey);
            else
                logger.trace("Retrying {}", configKey);
        }

        @Override
        protected IOException logIOException(
                final String configKey,
                final Level logLevel,
                final IOException ioe,
                final long elapsedTime) {
            logger.error("Failed {} after {} ms: {}", configKey,
                    elapsedTime, ioe, ioe);
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
                final ObjectMapper objectMapper,
                final LoggyProperties loggy) {
            super(logbook, logger, loggy);
            this.objectMapper = objectMapper;
        }

        private static void withJackson(final JacksonCall call) {
            try {
                call.call();
            } catch (final JsonProcessingException e) {
                throw new Bug("Jackson missing or misconfigured", e);
            }
        }

        @Override
        protected void log(final String configKey, final String format,
                final Object... args) {
            withJackson(() -> logger.trace(objectMapper.writeValueAsString(
                    new Log(configKey, format(format, args)))));
        }

        @Override
        protected void logRetry(
                final String configKey, final Level logLevel) {
            withJackson(() -> {
                if (loggy.isLogFeignRetries())
                    logger.warn(objectMapper.writeValueAsString(
                            new Retrying(configKey)));
                else
                    logger.trace(objectMapper.writeValueAsString(
                            new Retrying(configKey)));
            });
        }

        @Override
        protected IOException logIOException(
                final String configKey,
                final Level logLevel,
                final IOException ioe,
                final long elapsedTime) {
            withJackson(() -> logger.error(objectMapper.writeValueAsString(
                    new IoException(configKey, elapsedTime,
                            ioe.toString())),
                    ioe));

            return super.logIOException(
                    configKey, logLevel, ioe, elapsedTime);
        }

        @FunctionalInterface
        private interface JacksonCall {
            void call()
                    throws JsonProcessingException;
        }

        @Value
        private static class Log {
            String message = "log";
            String configKey;
            String log;
        }

        @Value
        private static class Retrying {
            String message = "retrying";
            String configKey;
        }

        @Value
        private static class IoException {
            String message = "ioException";
            String configKey;
            long elapsedTime;
            String exception;
        }
    }
}
