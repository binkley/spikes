package hm.binkley;

import static java.lang.System.out;

/**
 * See https://youtu.be/1s0w_p5HEuY
 */
public class Cell<T extends Comparable<T>> {
    public static void main(String... args) {
        p("==TRIPLE REF");
        p("---- START - CARROT");
        var first = new Cell<>("carrot");
        p(first);
        var head = first.ref();
        p("---- FIRST - APPLE");
        insert(head, new Cell<>("apple"));
        p(head.deref);
        p("---- MIDDLE - BANANA");
        insert(head, new Cell<>("banana"));
        p(head.deref);
        p("---- LAST - DILL");
        insert(head, new Cell<>("dill"));
        p(head.deref);
    }

    private Cell(T value) {
        this.value = value;
    }

    public T value;
    public Cell<T> next = null;

    public Ptr<Cell<T>> ref() {
        return new Ptr<>(this);
    }

    @Override
    public String toString() {
        return value + "->" + next;
    }

    public static <T extends Comparable<T>> void insert(Ptr<Cell<T>> head,
            Cell<T> item) {
        Ptr<Cell<T>> p;
        for (p = head; null != p.deref; p = new Ptr<>(p.deref.next)) {
            p("- *P -> " + p);
            if (item.value.compareTo(p.deref.value) < 1) {
                p("- BREAK " + item.value + " <= " + p.deref.value);
                break;
            }
        }

        p("- *P -> " + p);
        item.next = p.deref;
        p("- ITEM -> " + item);
        p.deref = item;
        p("- *P -> " + p);
        p("- *HEAD -> " + head);
    }

    private static void p(Object o) {
        out.println(o);
    }

    private static class Ptr<T> {
        private T deref;

        Ptr(T deref) {
            this.deref = deref;
        }

        @Override
        public String toString() {
            return deref + " @" + hashCode();
        }
    }
}
