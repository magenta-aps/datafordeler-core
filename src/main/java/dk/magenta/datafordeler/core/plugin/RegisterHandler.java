package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Checksum;
import dk.magenta.datafordeler.core.ChecksumInputStream;
import dk.magenta.datafordeler.core.Receipt;
import dk.magenta.datafordeler.core.event.BusinessEvent;
import dk.magenta.datafordeler.core.event.DataEvent;
import dk.magenta.datafordeler.core.exception.DataFordelerException;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by lars on 11-01-17.
*/
public abstract class RegisterHandler {



    /**
     * @return A URL to send the given receipt to
     * Depending on the register, the URL could change between receipts (such as the objectID being part of it)
     */
    protected abstract URL getReceiptInterface(Receipt receipt);

    public final void sendReceipt(Receipt receipt) {
        this.sendReceipts(Collections.singletonList(receipt));
    }

    public final void sendReceipts(List<Receipt> receipts) {
        // Send receipts to the register interface at the address pointed to by getReceiptInterface()
    }






    /**
     * @return a URL to call for fetching the checksum map
     */
    protected abstract URL getChecksumInterface();

    /**
     * Parse the response contents into Checksum instances
     * @param responseContent
     * @return Stream of Checksum instances for further processing
     */
    protected abstract ChecksumInputStream parseChecksumResponse(InputStream responseContent) throws DataFordelerException;

    public final ChecksumInputStream listRegisterChecksums(Date fromDate) throws DataFordelerException {
        // Request checksums from the register pointed to by getChecksumInterface()
        // Put the response through parseChecksumResponse
        return null;
    }






    /**
     * @return a URL to call for fetching registrations
     * Depending on the register, the URL could change between registrations (such as the checksum being part of it)
     */
    protected abstract URL getRegistrationsInterface(Checksum checksum);

    public final List<Version> getRegistrations(Collection<Checksum> checksums) throws DataFordelerException {
        // Request registrations from the register pointed to by getRegistrationsInterface()
        // TODO: Should this return a stream of instances?
        return null;
    }






    public List<Checksum> listLocalChecksums(Date fromDate) throws DataFordelerException {
        // Look in the local database for checksums
        // TODO: Should we return a Map instead, for quicker lookup?
        return null;
    }






    protected abstract void processBusinessEvent(BusinessEvent event);

    protected abstract void processDataEvent(DataEvent event);

    public final void synchronize() {
        this.synchronize(null);
    }

    public final void synchronize(Date fromdate) {
        // TODO: Likely move this to the dk.magenta.datafordeler.engine.Engine class
        // Fetch list of checksums with listRegisterChecksums
        // Find missing/mismatching versions
        // Fetch these versions
        // Update DB
        // Send receipts
    }

}
