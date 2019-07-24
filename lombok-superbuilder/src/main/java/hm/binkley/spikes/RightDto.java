package hm.binkley.spikes;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.net.URI;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public class RightDto
        extends BaseDto {
    URI somethingElse;
}
