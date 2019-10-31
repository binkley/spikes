package x.scratch.parent;

import javax.annotation.Nonnull;

public interface ParentDetails
        extends Comparable<ParentDetails> {
    @Nonnull
    String getNaturalId();

    String getValue();

    int getVersion();

    @Override
    default int compareTo(@Nonnull final ParentDetails that) {
        return getNaturalId().compareTo(that.getNaturalId());
    }
}
