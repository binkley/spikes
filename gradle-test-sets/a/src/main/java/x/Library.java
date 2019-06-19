package x;

public class Library {
    public boolean someLibraryMethod(final DependOnMe dependable) {
        dependable.show();
        return true;
    }
}
