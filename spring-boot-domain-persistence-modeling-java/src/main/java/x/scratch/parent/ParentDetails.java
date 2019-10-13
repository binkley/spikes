package x.scratch.parent;

import javax.annotation.Nonnull;

public interface ParentDetails
        extends Comparable<Parent> {
    @Nonnull
    String getNaturalId();

    String getValue();

    int getVersion();

    @Override
    default int compareTo(@Nonnull final Parent that) {
        return getNaturalId().compareTo(that.getNaturalId());
    }
}
