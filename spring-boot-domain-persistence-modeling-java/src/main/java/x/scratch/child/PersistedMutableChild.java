package x.scratch.child;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import x.scratch.TrackedSortedSet;

import javax.annotation.Nonnull;
import java.util.Set;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public final class PersistedMutableChild implements MutableChild {
    private final @NonNull ChildRecord record;

    @Nonnull
    @Override
    public String getNaturalId() {
        return record.getNaturalId();
    }

    @Override
    public String getParentNaturalId() {
        return record.getParentNaturalId();
    }

    @Override
    public void setParentNaturalId(final String parentNaturalId) {
        record.setParentNaturalId(parentNaturalId);
    }

    @Override
    public String getValue() {
        return record.getValue();
    }

    @Override
    public void setValue(final String newValue) {
        record.setValue(newValue);
    }

    @Nonnull
    @Override
    public Set<String> getSubchildren() {
        return new TrackedSortedSet<>(record.getSubchildren(),
                this::resetSubchildrenToPreserveSorting, this::resetSubchildrenToPreserveSorting);
    }

    @Override
    public int getVersion() {
        return record.getVersion();
    }

    private void resetSubchildrenToPreserveSorting(
            final String item, final Set<String> all) {
        record.getSubchildren().clear();
        record.getSubchildren().addAll(all);
    }
}
