package hm.binkley.spikes;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public class LeftDto
        extends BaseDto {
    boolean onTheLeft;
}
