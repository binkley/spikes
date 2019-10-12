package x.scratch;

import lombok.NonNull;
import lombok.Value;

@Value
public final class ParentResource {
    private final @NonNull String naturalId;
    private final String value;
    private final int version;
}
