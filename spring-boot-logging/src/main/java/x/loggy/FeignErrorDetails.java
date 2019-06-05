package x.loggy;

import feign.Request.HttpMethod;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
@ToString(callSuper = true)
public class FeignErrorDetails
        extends RuntimeException {
    HttpMethod method;
    String url;

    public FeignErrorDetails(final HttpMethod method, final String url) {
        super("SUPPRESSED SYNTHETIC EXCEPTION TO PASS FEIGN ERROR DETAILS");
        this.method = method;
        this.url = url;
    }
}
