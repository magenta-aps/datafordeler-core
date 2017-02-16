package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.exception.DataFordelerException;

import java.util.Date;

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
    private Date received;
    private String errorCode;
    private String errorMessage;

    public Receipt(String objectID, Date received) {
        this.objectID = objectID;
        this.status = Status.ok;
        this.received = received;
    }

    public Receipt(String objectID, Date received, DataFordelerException e) {
        this(objectID, received);
        this.status = Status.failed;
        this.errorCode = e.getCode();
        this.errorMessage = e.getMessage();
    }

}
