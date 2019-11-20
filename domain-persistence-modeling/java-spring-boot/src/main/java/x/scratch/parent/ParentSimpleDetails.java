package x.scratch.parent;

import javax.annotation.Nonnull;

public interface ParentSimpleDetails
        extends Comparable<ParentSimpleDetails> {
    @Nonnull
    String getNaturalId();

    String getValue();

    int getVersion();

    @Override
    default int compareTo(@Nonnull final ParentSimpleDetails that) {
        return getNaturalId().compareTo(that.getNaturalId());
    }
}
