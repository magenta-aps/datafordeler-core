package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.event.Reference;
import org.apache.http.StatusLine;

/**
 * Created by lars on 14-03-17.
 */
public class InvalidReferenceException extends DataFordelerException {

    private String reference;

    public InvalidReferenceException(String reference) {
        this.reference = reference;
    }

    @Override
    public String getCode() {
        return "datafordeler.reference_invalid";
    }

}
