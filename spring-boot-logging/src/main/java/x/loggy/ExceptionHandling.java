package x.loggy;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace.ALWAYS;
import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.UNPROCESSABLE_ENTITY;
import static x.loggy.AlertMessage.MessageFinder.findAlertMessage;

@ControllerAdvice
@Import(ProblemConfiguration.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExceptionHandling
        implements ProblemHandling {
    private final ServerProperties server;
    private final Logger logger;
    private final Alerter alerter;

    @Override
    public ResponseEntity<Problem> handleMessageNotReadableException(
            final HttpMessageNotReadableException exception,
            final NativeWebRequest request) {
        if (exception.getCause() instanceof MismatchedInputException)
            return handleMismatchedInputException(
                    (MismatchedInputException) exception.getCause(), request);

        return create(BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(MismatchedInputException.class)
    public ResponseEntity<Problem> handleMismatchedInputException(
            final MismatchedInputException e,
            final NativeWebRequest request) {
        return newConstraintViolationProblem(e, singleton(createViolation(
                new FieldError("n/a", jsonFieldPath(e),
                        e.getTargetType().getName() + ": "
                                + getMostSpecificCause(e).getMessage()))),
                request);
    }

    private static String jsonFieldPath(final MismatchedInputException e) {
        final var parts = e.getPath();
        if (parts.isEmpty())
            throw new Bug("JSON parsing failed without any JSON", e);

        final var buffer = new StringBuilder();
        for (final var part : parts) {
            final var fieldName = part.getFieldName();
            if (null == fieldName)
                buffer.append('[').append(part.getIndex()).append(']');
            else
                buffer.append('.').append(fieldName);
        }

        if ('.' == buffer.charAt(0))
            buffer.deleteCharAt(0);

        return buffer.toString();
    }

    @Override
    public boolean isCausalChainsEnabled() {
        return includeStackTrace(server);
    }

    static boolean includeStackTrace(final ServerProperties server) {
        return ALWAYS == server.getError().getIncludeStacktrace();
    }

    @Override
    public void log(
            @NonNull final Throwable throwable,
            final Problem problem,
            final NativeWebRequest request,
            final HttpStatus status) {
        final var alertMessage = findAlertMessage(throwable);
        if (null != alertMessage)
            alerter.alert(alertMessage, extra(throwable, status, request));

        final var realRequest = request
                .getNativeRequest(HttpServletRequest.class);
        if (null == realRequest)
            throw new Bug("No HTTP request");
        final var requestURL = realRequest.getRequestURL();

        if (status.is4xxClientError()) {
            logger.warn("{}: {}: {}",
                    status.getReasonPhrase(),
                    requestURL,
                    throwable.getMessage());
        } else if (status.is5xxServerError()) {
            logger.error("{}: {}: {}",
                    status.getReasonPhrase(),
                    requestURL,
                    throwable.getMessage(),
                    throwable);
        }
    }

    private static Map<String, Object> extra(
            final Throwable throwable,
            final HttpStatus status,
            final NativeWebRequest request) {
        final var extra = new LinkedHashMap<String, Object>(5);
        final var rootCause = getMostSpecificCause(throwable);
        extra.put("code-exception", rootCause);
        extra.put("code-location", codeLocation(rootCause));
        extra.put("response-status", status.value());
        extra.put("request-method", method(request));
        extra.put("request-url", url(request));
        return extra;
    }

    private static String codeLocation(final Throwable rootCause) {
        return Stream.of(rootCause.getStackTrace())
                .filter(ExceptionHandling::isApplicationCode)
                .findFirst()
                .map(Object::toString)
                .orElse("NONE");
    }

    private static String method(final NativeWebRequest original) {
        final var request = original
                .getNativeRequest(HttpServletRequest.class);
        return null == request ? "NONE" : request.getMethod();
    }

    private static String url(final NativeWebRequest original) {
        final var request = original
                .getNativeRequest(HttpServletRequest.class);
        if (null == request) return "NONE";
        final var url = request.getRequestURL();
        final var query = request.getQueryString();
        if (null != query) url.append('?').append(query);
        return url.toString();
    }

    private static boolean isApplicationCode(final StackTraceElement frame) {
        final var className = frame.getClassName();
        return className.startsWith("x.loggy.")
                && !className.equals(LoggyErrorDecoder.class.getName());
    }

    @Override
    public StatusType defaultConstraintViolationStatus() {
        return UNPROCESSABLE_ENTITY;
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Problem> handleFeignException(
            final FeignException e, final NativeWebRequest request) {
        final var rootException = getMostSpecificCause(e);
        final var message = e.equals(rootException)
                ? e.toString()
                : e + ": " + rootException;
        final var status = 400 <= e.status() && e.status() < 500
                ? INTERNAL_SERVER_ERROR
                : BAD_GATEWAY;
        final var problem = Problem.builder()
                .withDetail(message)
                .withStatus(status);

        problem.with("feign-status", e.status());
        final var details = findRequestDetails(e);
        if (null != details) problem
                .with("feign-method", details.getMethod().name())
                .with("feign-url", details.getUrl());

        return create(e, problem.build(), request);
    }

    private static FeignErrorDetails findRequestDetails(
            final Throwable throwable) {
        for (Throwable x = throwable; null != x; x = x.getCause())
            for (final Throwable suppressed : x.getSuppressed())
                if (suppressed instanceof FeignErrorDetails)
                    return (FeignErrorDetails) suppressed;
        return null;
    }
}
