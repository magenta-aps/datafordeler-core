package dk.magenta.datafordeler.core.plugin;

import java.util.Date;

/**
 * Created by lars on 11-01-17.
 */
public abstract class BaseRegisterHandler {

    public abstract void sendReceipt();

    protected abstract void processBusinessEvent();

    protected abstract void processDataEvent();

    public void sync(Date fromdate) {
        // Fetch list of checksums
        // Find missing/mismatching versions
        // Fetch these versions
        // Update DB
    }
}
