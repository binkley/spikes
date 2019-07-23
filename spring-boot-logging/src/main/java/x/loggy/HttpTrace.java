package x.loggy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import x.loggy.HttpTrace.RequestTrace;
import x.loggy.HttpTrace.ResponseTrace;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(exclude = "correlation") // TODO: Needed?
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(RequestTrace.class),
        @JsonSubTypes.Type(ResponseTrace.class)
})
public class HttpTrace {
    public String origin;
    public String type;
    public String correlation;
    public String protocol;
    public Map<String, List<String>> headers;
    public JsonNode body;

    public static HttpTrace httpTraceOf(final ObjectMapper objectMapper,
            final String logMessage) {
        try {
            return objectMapper.readValue(logMessage, HttpTrace.class);
        } catch (final IOException e) {
            throw new IOError(e);
        }
    }

    @JsonIgnore
    public boolean isProblem() {
        return headers.containsValue(List.of("application/problem+json"));
    }

    public <T> T getBodyAs(
            final Class<T> type, final ObjectMapper objectMapper) {
        try {
            return objectMapper.treeToValue(body, type);
        } catch (final JsonProcessingException e) {
            throw new Bug("Not a " + type.getName() + ": " + body + ": " + e,
                    e);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @JsonTypeName("request")
    @ToString(callSuper = true)
    public static class RequestTrace
            extends HttpTrace {
        public String remote;
        public String method;
        public URI uri;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @JsonTypeName("response")
    @ToString(callSuper = true)
    public static class ResponseTrace
            extends HttpTrace {
        public int duration;
        public int status;
    }
}
