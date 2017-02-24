package dk.magenta.datafordeler.core.exception;

/**
 * Created by lars on 16-02-17.
 */
public abstract class DataFordelerException extends Exception {

    public DataFordelerException() {
        super();
    }

    public DataFordelerException(String message) {
        super(message);
    }

    public DataFordelerException(Throwable cause) {
        super(cause);
    }

    public DataFordelerException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getCode();
}
