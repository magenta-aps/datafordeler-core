package dk.magenta.datafordeler.core.exception;

import org.apache.http.StatusLine;

/**
 * An exception that basically tells that we got an HTTP status that we didn't expect
 *
 */
public class HttpStatusException extends DataFordelerException {

    private StatusLine statusLine;

    public HttpStatusException(StatusLine statusLine) {
        this.statusLine = statusLine;
    }

    @Override
    public String getCode() {
        return null;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }
}
