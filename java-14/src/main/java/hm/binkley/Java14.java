package hm.binkley;

import static java.lang.System.out;

public class Java14 {
    public static void main(String[] args) {
        final var x = "bob";
        if (x instanceof String msg) out.println(new Fooby<>(3, msg));
    }
}

record Fooby<T>(int i, T s) {
    public Fooby {
        if (i < 0 || null == s) throw new IllegalArgumentException();
    }
}
