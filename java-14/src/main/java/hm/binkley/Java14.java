package hm.binkley;

public class Java14 {
    public static void main(String[] args) {
        System.out.println(new Fooby<>(3, "bob"));
    }
}

record Fooby<T>(int i, T s) {
    public Fooby {
        if (i < 0 || null == s) throw new IllegalArgumentException();
    }
}
