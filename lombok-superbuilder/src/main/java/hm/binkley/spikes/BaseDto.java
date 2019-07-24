package hm.binkley.spikes;

import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public abstract class BaseDto {
    String text;
    int number;
}
