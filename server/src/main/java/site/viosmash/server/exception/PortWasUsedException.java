package site.viosmash.server.exception;

public class PortWasUsedException extends RuntimeException {
    public PortWasUsedException(String message) {
        super(message);
    }
}
