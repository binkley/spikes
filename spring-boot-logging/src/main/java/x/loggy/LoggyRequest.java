package x.loggy;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Builder
@Value
public class LoggyRequest {
    @Min(1)
    @NotNull
    Integer blinkenLights;
    @NotNull
    LocalDate when;
}
