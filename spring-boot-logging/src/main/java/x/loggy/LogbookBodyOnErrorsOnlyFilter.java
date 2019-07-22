package x.loggy;

import org.zalando.logbook.ForwardingHttpRequest;
import org.zalando.logbook.ForwardingHttpResponse;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.ResponseFilter;

import javax.validation.constraints.NotNull;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LogbookBodyOnErrorsOnlyFilter
        implements RequestFilter, ResponseFilter {
    public static final String HIDDEN_BODY_AS_STRING = "<skipped>";
    // Same as Logbook
    static final byte[] HIDDEN_BODY = HIDDEN_BODY_AS_STRING.getBytes(UTF_8);

    @Override
    public HttpRequest filter(final @NotNull HttpRequest request) {
        return new ForwardingHttpRequest() {
            @Override
            public HttpRequest delegate() {
                return request;
            }

            @Override
            public byte[] getBody() {
                return HIDDEN_BODY;
            }

            @Override
            public String getBodyAsString() {
                return HIDDEN_BODY_AS_STRING;
            }
        };
    }

    @Override
    public HttpResponse filter(final @NotNull HttpResponse response) {
        return new ForwardingHttpResponse() {
            @Override
            public HttpResponse delegate() {
                return response;
            }

            @Override
            public byte[] getBody()
                    throws IOException {
                return response.getStatus() < 400
                        ? HIDDEN_BODY
                        : response.getBody();
            }

            @Override
            public String getBodyAsString()
                    throws IOException {
                return response.getStatus() < 400
                        ? HIDDEN_BODY_AS_STRING
                        : response.getBodyAsString();
            }
        };
    }
}
