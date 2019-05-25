package x.xmlish;

class Bug
        extends RuntimeException {
    Bug(final Throwable cause) {
        super("BUG: " + cause.getMessage(), cause);
    }
}
