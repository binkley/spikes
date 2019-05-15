package x.loggy;

import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
public class LoggyRequest {
    @Min(1)
    @NotNull
    Integer blinkenLights;
}
