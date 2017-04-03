package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.exception.DataFordelerException;

import java.time.OffsetDateTime;

/**
 * Created by lars on 16-02-17.
 */
public class Receipt {

    public enum Status {
        ok,
        failed
    }

    private String objectID;
    private Status status;
    private OffsetDateTime received;
    private String errorCode;
    private String errorMessage;

    public Receipt() {}

    public Receipt(String objectID, OffsetDateTime received) {
        this.objectID = objectID;
        this.status = Status.ok;
        this.received = received;
    }

    public Receipt(String objectID, OffsetDateTime received, DataFordelerException e) {
        this(objectID, received);
        this.status = Status.failed;
        this.errorCode = e.getCode();
        this.errorMessage = e.getMessage();
    }

    public String getObjectID() {
        return objectID;
    }

    public Status getStatus() {
        return status;
    }

    public OffsetDateTime getReceived() {
        return received;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
