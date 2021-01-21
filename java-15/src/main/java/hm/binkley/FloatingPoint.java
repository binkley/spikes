package hm.binkley;

import java.util.Arrays;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.System.out;
import static java.util.Arrays.sort;

public final class FloatingPoint {
    public static void main(final String... args) {
        final var da = new double[]{
                0.0, NaN, -1.0, NEGATIVE_INFINITY, NaN, 1.0, POSITIVE_INFINITY
        };

        out.println("BEFORE -> " + Arrays.toString(da));
        sort(da);
        out.println("AFTER -> " + Arrays.toString(da));
    }
}
