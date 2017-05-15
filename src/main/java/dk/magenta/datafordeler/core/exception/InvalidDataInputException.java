package dk.magenta.datafordeler.core.exception;

/**
 * Created by lars on 10-05-17.
 */
public abstract class InvalidDataInputException extends DataFordelerException {

    public InvalidDataInputException(String msg) {
        super(msg);
    }

}
