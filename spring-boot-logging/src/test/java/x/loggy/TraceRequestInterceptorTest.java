package x.loggy;

import brave.Span;
import brave.Tracing;
import brave.propagation.TraceContext;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class TraceRequestInterceptorTest {
    @Mock
    private final Logger logger;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldReuseContext() {
        final var tracing = Tracing.newBuilder().build();
        final var tracer = spy(tracing.tracer());
        final var currentSpan = mock(Span.class);
        doReturn(currentSpan)
                .when(tracer).currentSpan();
        final var currentContext = mock(TraceContext.class);
        doReturn(currentContext)
                .when(currentSpan).context();
        // And so the problems with mocks: A little too much knowledge about
        // the innards of the mocked class
        doReturn("deadbeef")
                .when(currentContext).traceIdString();
        final var interceptor = new TraceRequestInterceptor(
                tracing, tracer, logger);
        final var template = spy(new RequestTemplate());

        interceptor.apply(template);

        verify(tracer, never()).newTrace();
        verify(template).header("X-B3-TraceId", "deadbeef");
    }

    @Test
    void shouldCreateContext() {
        final var tracing = Tracing.newBuilder().build();
        final var tracer = spy(tracing.tracer());
        final var interceptor = new TraceRequestInterceptor(
                tracing, tracer, logger);
        final var template = spy(new RequestTemplate());

        interceptor.apply(template);

        verify(tracer).newTrace();
        verify(template).header(eq("X-B3-TraceId"), anyString());
    }
}
