package hm.binkley.spikes;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder(toBuilder = true)
public class Dto {
    String text;
    int number;
}
