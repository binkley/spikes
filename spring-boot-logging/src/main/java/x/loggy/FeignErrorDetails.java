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
    String alertMessage;
}
