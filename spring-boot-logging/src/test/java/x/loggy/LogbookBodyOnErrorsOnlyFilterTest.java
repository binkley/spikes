package x.loggy;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static x.loggy.LogbookBodyOnErrorsOnlyFilter.HIDDEN_BODY;
import static x.loggy.LogbookBodyOnErrorsOnlyFilter.HIDDEN_BODY_AS_STRING;

class LogbookBodyOnErrorsOnlyFilterTest {
    private static final LogbookBodyOnErrorsOnlyFilter filter =
            new LogbookBodyOnErrorsOnlyFilter();

    @Test
    void shouldHideBodyForAllRequestsUntilWeWorkOutHowToReactToResponseStatus()
            throws
            IOException {
        final var before = mock(HttpRequest.class);
        final var bodyContent = "PAYLOAD";
        lenient().when(before.getBody())
                .thenReturn(bodyContent.getBytes(UTF_8));
        lenient().when(before.getBodyAsString()).thenReturn(bodyContent);

        final HttpRequest after = filter.filter(before);

        assertThat(after.getBody()).isEqualTo(HIDDEN_BODY);
        assertThat(after.getBodyAsString()).isEqualTo(HIDDEN_BODY_AS_STRING);
    }

    @Test
    void shouldHideBodyForSuccessfulResponse()
            throws IOException {
        final var before = mock(HttpResponse.class);
        when(before.getStatus()).thenReturn(OK.value());
        final var bodyContent = "PAYLOAD";
        lenient().when(before.getBody())
                .thenReturn(bodyContent.getBytes(UTF_8));
        lenient().when(before.getBodyAsString()).thenReturn(bodyContent);

        final HttpResponse after = filter.filter(before);

        assertThat(after.getBody()).isEqualTo(HIDDEN_BODY);
        assertThat(after.getBodyAsString()).isEqualTo(HIDDEN_BODY_AS_STRING);
    }

    @Test
    void shouldShowBodyForFailResponse()
            throws IOException {
        final var before = mock(HttpResponse.class);
        when(before.getStatus()).thenReturn(BAD_REQUEST.value());
        final String bodyContent = "FAIL BODY";
        when(before.getBody()).thenReturn(bodyContent.getBytes(UTF_8));
        when(before.getBodyAsString()).thenCallRealMethod();
        when(before.getCharset()).thenReturn(UTF_8);

        final HttpResponse after = filter.filter(before);

        assertThat(after.getBody()).isEqualTo(bodyContent.getBytes(UTF_8));
        assertThat(after.getBodyAsString()).isEqualTo(bodyContent);
    }
}
