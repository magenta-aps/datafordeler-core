package dk.magenta.datafordeler.core.exception;

import java.io.File;
import java.util.List;

public class ImportInterruptedException extends DataFordelerException {

    public ImportInterruptedException(InterruptedException cause) {
        super(cause);
    }

    private Long chunk = null;

    public Long getChunk() {
        return this.chunk;
    }

    public void setChunk(long chunk) {
        this.chunk = chunk;
    }

    private List<File> files;

    public List<File> getFiles() {
        return this.files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.interrupted";
    }
}
