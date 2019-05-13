package x.loggy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import x.loggy.HttpTrace.RequestTrace;
import x.loggy.HttpTrace.ResponseTrace;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(RequestTrace.class),
        @JsonSubTypes.Type(ResponseTrace.class)
})
public class HttpTrace {
    protected String origin;
    protected String type;
    protected String correlation;
    protected String protocol;
    protected Map<String, List<String>> headers;

    @Value
    @EqualsAndHashCode(callSuper = true)
    @JsonTypeName("request")
    @ToString(callSuper = true)
    public static class RequestTrace
            extends HttpTrace {
        String remote;
        String method;
        URI uri;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @JsonTypeName("response")
    @ToString(callSuper = true)
    public static class ResponseTrace
            extends HttpTrace {
        int duration;
        int status;
        JsonNode body;
    }
}
