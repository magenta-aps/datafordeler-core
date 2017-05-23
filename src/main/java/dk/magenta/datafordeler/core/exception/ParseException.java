package dk.magenta.datafordeler.core.exception;

/**
 * Created by lars on 18-05-17.
 */
public class ParseException extends InvalidDataInputException {

    public ParseException(String msg) {
        super(msg);
    }

    public ParseException(String msg, Throwable cause) {
        super(msg);
        this.initCause(cause);
    }

    @Override
    public String getCode() {
        return null;
    }
}
