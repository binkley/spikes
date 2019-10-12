package x.scratch.child;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public final class PersistedMutableChild implements MutableChild {
    @Delegate(types = MutableChild.class)
    private final @NonNull ChildRecord record;
}
