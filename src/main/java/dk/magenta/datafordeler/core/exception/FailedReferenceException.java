package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.event.Reference;
import org.apache.http.StatusLine;

/**
 * Created by lars on 14-03-17.
 */
public class FailedReferenceException extends HttpStatusException {

    private Reference reference;

    public FailedReferenceException(Reference reference, StatusLine statusLine) {
        super(statusLine);
        this.reference = reference;
    }

    public FailedReferenceException(Reference reference, HttpStatusException cause) {
        this(reference, cause.getStatusLine());
        this.initCause(cause);
    }
}
