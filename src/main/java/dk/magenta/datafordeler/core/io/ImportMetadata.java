package dk.magenta.datafordeler.core.io;

import java.time.OffsetDateTime;

public class ImportMetadata {

    public ImportMetadata() {
        this.importTime = OffsetDateTime.now();
    }

    private OffsetDateTime importTime;

    public OffsetDateTime getImportTime() {
        return this.importTime;
    }

    public void setImportTime(OffsetDateTime importTime) {
        this.importTime = importTime;
    }
}
