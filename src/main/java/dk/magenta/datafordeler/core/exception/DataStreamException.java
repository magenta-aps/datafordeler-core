package dk.magenta.datafordeler.core.exception;

import java.io.IOException;

/**
 * Created by lars on 14-03-17.
 */
public class DataStreamException extends DataFordelerException {

    public DataStreamException(IOException cause) {
        super(cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.ioexception";
    }
}
