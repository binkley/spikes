package x.scratch.parent;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public final class PersistedMutableParent
        implements MutableParent {
    @Delegate(types = MutableParentDetails.class)
    private final @NonNull ParentRecord record;
}
