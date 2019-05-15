package x.loggy;

public class Bug
        extends RuntimeException {
    public Bug(final String message) {
        this(message, null);
    }

    public Bug(final String message, final Throwable cause) {
        super("BUG: " + message, cause);
    }
}
