package dk.magenta.datafordeler.core.exception;

import org.apache.http.StatusLine;

/**
 * Created by lars on 14-03-17.
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
