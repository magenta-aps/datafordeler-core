package dk.magenta.datafordeler.core.exception;

/**
 * Created by lars on 07-03-17.
 */
public class MissingReferenceException extends DataFordelerException {
    @Override
    public String getCode() {
        return "datafordeler.engine.oldevent.missing_reference";
    }
}
