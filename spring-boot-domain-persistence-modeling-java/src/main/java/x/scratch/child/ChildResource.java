package x.scratch.child;

import lombok.NonNull;
import lombok.Value;

@Value
public final class ChildResource {
    private final @NonNull String naturalId;
    private final String parentNaturalId;
    private final String value;
    private final int version;
}
