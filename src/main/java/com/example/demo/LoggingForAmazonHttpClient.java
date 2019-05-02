package com.example.demo;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.Request;
import com.amazonaws.Response;
import lombok.RequiredArgsConstructor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.style.ToStringCreator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggingForAmazonHttpClient
        implements Answer<Response<?>> {
    private final Logger logger;

    @Override
    public Response<?> answer(final InvocationOnMock invocation)
            throws Throwable {
        final Request<?> request = invocation.getArgument(0);
        try {
            final var response
                    = (Response<?>) invocation.callRealMethod();
            logger.debug("AWS HTTP: {}: {}",
                    request, response.getAwsResponse());
            return response;
        } catch (final AmazonServiceException e) {
            final var eToString = new ToStringCreator(e)
                    .append("errorCode", e.getErrorCode())
                    .append("errorMessage", e.getErrorMessage())
                    .append("errorType", e.getErrorType())
                    .append("statusCode", e.getStatusCode())
                    .append("serviceName", e.getServiceName())
                    .toString();
            logger.error("AWS HTTP service failed: {}: {}",
                    request, eToString, e);
            throw e;
        } catch (final AmazonClientException e) {
            logger.error("AWS HTTP client failed: {}: {}",
                    request, e.toString(), e);
            throw e;
        }
    }
}
