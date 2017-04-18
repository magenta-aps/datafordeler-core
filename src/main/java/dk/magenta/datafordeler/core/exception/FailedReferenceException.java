package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.RegistrationReference;
import org.apache.http.StatusLine;

/**
 * Created by lars on 14-03-17.
 */
public class FailedReferenceException extends HttpStatusException {

    private RegistrationReference reference;

    public FailedReferenceException(RegistrationReference reference, StatusLine statusLine) {
        super(statusLine);
        this.reference = reference;
    }

    public FailedReferenceException(RegistrationReference reference, HttpStatusException cause) {
        super(cause.getStatusLine(), cause.getUri());
        this.reference = reference;
        this.initCause(cause);
    }
}
