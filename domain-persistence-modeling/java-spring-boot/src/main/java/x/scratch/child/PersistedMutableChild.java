package x.scratch.child;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;
import x.scratch.TrackedSortedSet;
import x.scratch.parent.ParentSimpleDetails;

import javax.annotation.Nonnull;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static lombok.AccessLevel.PACKAGE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PACKAGE)
@ToString
final class PersistedMutableChild
        implements MutableChild {
    @Delegate(types = JavaWorkaroundMutableChildSimpleDetails.class)
    private final ChildRecord record;

    @Override
    public void setValue(final String newValue) {
        record.setValue(newValue);
    }

    @Nonnull
    @Override
    public Set<String> getDefaultSideValues() {
        return unmodifiableSet(record.getDefaultSideValues());
    }

    @Nonnull
    @Override
    public Set<String> getSideValues() {
        return new TrackedSortedSet<>(record.getSideValues(),
                (item, all) -> record.setSideValues(all),
                (item, all) -> record.setSideValues(all));
    }

    @Override
    public void assignTo(final ParentSimpleDetails parent) {
        record.setParentNaturalId(parent.getNaturalId());
    }

    @Override
    public void unassignFromAny() {
        record.setParentNaturalId(null);
    }

    private interface JavaWorkaroundMutableChildSimpleDetails {
        @Nonnull
        String getNaturalId();

        String getParentNaturalId();

        String getValue();

        int getVersion();
    }
}
