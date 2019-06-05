package x.validating;

class Bug
        extends RuntimeException {
    Bug(final Throwable cause) {
        super("BUG: " + cause.getMessage(), cause);
    }
}
