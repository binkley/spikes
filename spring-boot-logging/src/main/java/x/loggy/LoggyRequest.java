package x.loggy;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Value
public class LoggyRequest {
    @Min(1)
    @NotNull
    Integer blinkenLights;
    @NotEmpty
    @Valid
    List<Rolly> rollies;

    @Value
    public static class Rolly {
        @NotNull
        LocalDate when;
    }
}
