package dk.magenta.datafordeler.core.exception;

/**
 * Created by lars on 18-05-17.
 */
public class ParseException extends InvalidDataInputException {
    public ParseException(String msg) {
        super(msg);
    }

    @Override
    public String getCode() {
        return null;
    }
}
