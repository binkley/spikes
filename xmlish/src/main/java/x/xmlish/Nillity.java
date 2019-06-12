package x.xmlish;

import lombok.Value;

import java.util.List;

@Value
class Nillity {
    Outer outer;

    @Value
    static class Outer {
        Upper upper;
        List<Inner> inner;

        @Value
        static class Upper {
            String foo;
            Integer bar;
        }

        @Value
        static class Inner {
            String qux;
            Integer quux;
        }
    }
}
