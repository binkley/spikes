package hello.world;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
public class SampleData {
    private @NotEmpty String a;
    private @Positive int b;
}
