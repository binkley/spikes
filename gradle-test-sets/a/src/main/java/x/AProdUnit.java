package x;

public class AProdUnit {
    public String doIt(final DependOnMe dependable) {
        return dependable.tell();
    }
}
