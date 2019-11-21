package x.scratch.child;

import lombok.NonNull;
import lombok.Value;

import java.util.Set;

@Value
public final class ChildSnapshot {
    private final @NonNull String naturalId;
    private final String parentNaturalId;
    private final String value;
    private final Set<String> defaultSideValues;
    private final Set<String> sideValues;
    private final int version;
}
