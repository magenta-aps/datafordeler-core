package dk.magenta.datafordeler.core.exception;

/**
 * Created by lars on 01-06-17.
 */
public class SimilarJobRunningException extends DataFordelerException {

    public SimilarJobRunningException(String message) {
        super(message);
    }

    @Override
    public String getCode() {
        return "datafordeler.engine.similarjobrunning";
    }
}
