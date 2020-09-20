package hm.binkley;

import java.util.Comparator;
import java.util.List;

import static hm.binkley.MinMax.minMax;
import static java.lang.System.out;

public class Java15 {
    public static void main(String[] args) {
        Object x = 7;
        final var javaVersion = System.getProperty("java.version");
        if (null != javaVersion) x = """
                Uncle
                Bob says: %s""".formatted(javaVersion);
        if (x instanceof String msg) out.println(new Fooby<>(3, msg));

        out.println("minMax = " +
                minMax(List.of(3, 2, 1, 5), Integer::compareTo));
    }
}

record Fooby<T>(int i, T s) {
    public Fooby {
        if (i < 0 || null == s) throw new IllegalArgumentException();
    }
}

record MinMax<T>(T min, T max) {
    static <T> MinMax<T> minMax(final Iterable<T> iterable,
            final Comparator<T> comparator) {
        final var it = iterable.iterator();
        if (!it.hasNext()) throw new IllegalArgumentException();
        var min = it.next();
        var max = min;
        while (it.hasNext()) {
            var next = it.next();
            if (0 > comparator.compare(next, min)) min = next;
            else if (0 < comparator.compare(next, max)) max = next;
        }
        return new MinMax<>(min, max);
    }
}
