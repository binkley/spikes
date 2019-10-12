package x.scratch.parent;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public final class PersistedMutableParent implements MutableParent {
    private final @NonNull ParentRecord record;

    @Override
    public String getNaturalId() {
        return record.getNaturalId();
    }

    @Override
    public String getValue() {
        return record.getValue();
    }

    @Override
    public void setValue(final String newValue) {
        record.setValue(newValue);
    }

    @Override
    public int getVersion() {
        return record.getVersion();
    }
}
