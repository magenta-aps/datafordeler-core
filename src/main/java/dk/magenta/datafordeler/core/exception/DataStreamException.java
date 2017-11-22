package dk.magenta.datafordeler.core.exception;

import java.io.IOException;

/**
 * Created by lars on 14-03-17.
 */
public class DataStreamException extends DataFordelerException {

    public DataStreamException(Exception cause) {
        super(cause);
    }

    public DataStreamException(String message) {
        super(message);
    }

    public DataStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.ioexception";
    }
}
