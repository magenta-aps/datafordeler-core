package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Receipt;
import dk.magenta.datafordeler.core.event.BusinessEvent;
import dk.magenta.datafordeler.core.event.DataEvent;

import java.net.URL;
import java.util.List;

/**
 * Created by lars on 11-01-17.
 */

public abstract class Plugin {

    protected long version = 1L;

    protected List<Class> entityClasses;

    protected RegisterHandler registerHandler;

    protected RolesDefinition roleDefinition;

    protected FieldsDefinition fieldsDefinition;

    public Plugin() {
    }

    public long getVersion() {
    return version;
    }

    public void processBusinessEvent(BusinessEvent event) {
        this.registerHandler.processBusinessEvent(event);
    }

    public void processDataEvent(DataEvent event) {
        this.registerHandler.processDataEvent(event);
    }


    /**
     * @return A URL to send the given receipt to
     * Depending on the register, the URL could change between receipts (such as the objectID being part of it)
     */
    protected abstract URL getReceiptInterface(Receipt receipt);

    public final void sendReceipts(List<Receipt> receipts) {
        // Send receipts to the register interface at the address pointed to by getReceiptInterface()
    }
}
