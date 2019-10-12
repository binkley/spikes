package x.scratch.child;

import javax.annotation.Nonnull;

public interface ChildDetails {
    @Nonnull
    String getNaturalId();

    String getParentNaturalId();

    String getValue();

    int getVersion();
}
