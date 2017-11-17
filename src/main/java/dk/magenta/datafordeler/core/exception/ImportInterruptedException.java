package dk.magenta.datafordeler.core.exception;

public class ImportInterruptedException extends DataFordelerException {

    public ImportInterruptedException(InterruptedException cause) {
        super(cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.import.interrupted";
    }
}
