package x.scratch.parent;

import javax.annotation.Nonnull;

public interface ParentDetails {
    @Nonnull
    String getNaturalId();

    String getValue();

    int getVersion();
}
