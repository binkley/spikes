package x.scratch;

import java.util.Set;

import static java.util.stream.Collectors.joining;

public final class Persistence {
    public static String workAroundArrayTypeForPostgres(
            final Set<String> it) {
        // TODO: Workaround issue in Spring Data with passing sets for
        //  ARRAY types in a procedure
        return it.stream().collect(joining(",", "{", "}"));
    }
}
