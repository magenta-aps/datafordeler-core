package dk.magenta.datafordeler.core.io;

import org.hibernate.Session;

import java.net.URI;
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



    private URI currentURI;

    public URI getCurrentURI() {
        return this.currentURI;
    }

    public void setCurrentURI(URI currentURI) {
        this.currentURI = currentURI;
    }



    private boolean transactionInProgress;

    public boolean isTransactionInProgress() {
        return this.transactionInProgress;
    }

    public void setTransactionInProgress(boolean transactionInProgress) {
        this.transactionInProgress = transactionInProgress;
    }
}
