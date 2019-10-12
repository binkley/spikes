package x.scratch.child;

import javax.annotation.Nonnull;
import java.util.Set;

public interface ChildDetails {
    @Nonnull
    String getNaturalId();

    String getParentNaturalId();

    String getValue();

    @Nonnull
    Set<String> getSubchildren();

    int getVersion();
}
