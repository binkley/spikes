package x.scratch.child;

import javax.annotation.Nonnull;
import java.util.Set;

public interface ChildSimpleDetails
        extends Comparable<ChildSimpleDetails> {
    @Nonnull
    String getNaturalId();

    String getParentNaturalId();

    String getValue();

    @Nonnull
    Set<String> getSubchildren();

    int getVersion();

    @Override
    default int compareTo(@Nonnull final ChildSimpleDetails that) {
        return getNaturalId().compareTo(that.getNaturalId());
    }
}
