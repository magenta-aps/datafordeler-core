package dk.magenta.datafordeler.core.io;

import org.hibernate.Session;

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



    private Session session;

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
