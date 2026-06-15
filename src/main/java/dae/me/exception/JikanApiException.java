package dae.me.exception;

public class JikanApiException extends RuntimeException {

    public JikanApiException(String message) {
        super(message);
    }

    public JikanApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
