package x.xmlish;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class OuterJaxb {
    public Upper upper;
    public List<Inner> inner;

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class Upper {
        public String foo;
        public Integer bar;
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class Inner {
        public String foo;
        public Integer quux;
    }
}
