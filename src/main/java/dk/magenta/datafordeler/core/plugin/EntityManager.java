package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.EntityReference;
import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.database.Registration;
import org.apache.http.StatusLine;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by lars on 13-03-17.
 *
 * Entity (and associates) specific manager. Subclass in plugins
 * A plugin can have any number of Entity classes, each needing their own way of handling
 */
public abstract class EntityManager {
    private RegisterManager registerManager;
    protected Class<? extends Entity> managedEntityClass;
    protected Class<? extends EntityReference> managedEntityReferenceClass;
    protected Class<? extends RegistrationReference> managedRegistrationReferenceClass;
    protected Class<? extends Registration> managedRegistrationClass;


    public Class<? extends Entity> getManagedEntityClass() {
        return this.managedEntityClass;
    }

    public Class<? extends EntityReference> getManagedEntityReferenceClass() {
        return this.managedEntityReferenceClass;
    }

    public Class<? extends RegistrationReference> getManagedRegistrationReferenceClass() {
        return this.managedRegistrationReferenceClass;
    }

    public Class<? extends Registration> getManagedRegistrationClass() {
        return this.managedRegistrationClass;
    }

    public abstract Collection<String> getHandledURISubstrings();

    /**
     * Plugins must return an autowired ObjectMapper instance from this method
     * @return
     */
    protected abstract ObjectMapper getObjectMapper();

    /**
     * Plugins must return a Fetcher instance from this method
     * @return
     */
    protected abstract Communicator getRegistrationFetcher();

    protected abstract Communicator getReceiptSender();

    /**
     * Plugins must return an instance of a FapiService subclass from this method
     * @return
     */
    public abstract FapiService getEntityService();



    public abstract URI getBaseEndpoint();

    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    public void setRegisterManager(RegisterManager registerManager) {
        this.registerManager = registerManager;
    }

    public abstract String getSchema();

    /** Receipt sending **/

    /**
     * @return A URL to send the given receipt to
     * Depending on the register, the URL could change between receipts (such as the eventID being part of it)
     */
    protected abstract URI getReceiptEndpoint(Receipt receipt);


    /**
     * Sends a receipt to the register. Plugins are free to overload this with their own implementation
     * @param receipt
     * @return
     * @throws IOException
     */
    public int sendReceipt(Receipt receipt) throws IOException {
        ObjectMapper objectMapper = this.getObjectMapper();
        URI receiptEndpoint = this.getReceiptEndpoint(receipt);
        if (receiptEndpoint != null) {
            String payload = objectMapper.writeValueAsString(receipt);
            StatusLine statusLine = this.getReceiptSender().send(receiptEndpoint, payload);
            this.getLog().info("Receipt sent to " + receiptEndpoint + ", response was: HTTP " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
            return statusLine.getStatusCode();
        }
        return 0;
    }

    /**
     * Sends multiple receipts, calling sendReceipt for each Receipt in the input
     * @param receipts
     * @return
     */
    public Map<Receipt, Integer> sendReceipts(List<Receipt> receipts) {
        // TODO: Pack receipts to the same URI together in one request
        HashMap<Receipt, Integer> responses = new HashMap<>();
        for (Receipt receipt : receipts) {
            int statuscode = 0;
            try {
                statuscode = this.sendReceipt(receipt);
            } catch (IOException e) {
                // What to do here?
            }
            responses.put(receipt, statuscode);
        }
        return responses;
    }



















    /** Reference parsing **/

    /**
     * Parse incoming data into a Reference (data coming from within a request envelope)
     * @param referenceData
     * @return
     * @throws IOException
     */
    public abstract RegistrationReference parseReference(InputStream referenceData) throws IOException;

    /**
     * Parse incoming data into a Reference (data coming from within a request envelope)
     * @param referenceData
     * @return
     * @throws IOException
     */
    public abstract RegistrationReference parseReference(String referenceData, String charsetName) throws IOException;

    /**
     * Parse incoming data into a Reference (data coming from within a request envelope)
     * @param uri
     * @return
     */
    public abstract RegistrationReference parseReference(URI uri);






    /** Registration parsing **/

    /**
     * Parse incoming data into a Registration (data coming from within a request envelope)
     * @param registrationData
     * @return
     * @throws IOException
     */
    public List<? extends Registration> parseRegistration(InputStream registrationData) throws ParseException, IOException {
        String data = new Scanner(registrationData,"UTF-8").useDelimiter("\\A").next();
        return this.parseRegistration(data);
    }

    /**
     * Parse incoming data into a Registration (data coming from within a request envelope)
     * @param registrationData
     * @return
     * @throws IOException
     */
    public List<? extends Registration> parseRegistration(String registrationData) throws ParseException, IOException {
        return this.parseRegistration(this.getObjectMapper().readTree(registrationData));
    }

    public List<? extends Registration> parseRegistration(JsonNode registrationData) throws ParseException {
        return null;
    }



    public Map<String, List<? extends Registration>> parseRegistrationList(JsonNode registrationData) throws ParseException {
        HashMap<String, List<? extends Registration>> registrationMap = new HashMap<>();
        Iterator<String> keyIterator = registrationData.fieldNames();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            List<? extends Registration> registrations = this.parseRegistration(registrationData.get(key));
            registrationMap.put(key, registrations);
        }
        return registrationMap;
    }

    /** Registration fetching **/

    /**
     *
     * @param reference
     * @return
     * @throws WrongSubclassException
     */
    public abstract URI getRegistrationInterface(RegistrationReference reference) throws WrongSubclassException;

    /**
     * Obtain a Registration by Reference
     * @param reference
     * @return Registration object, fetched and parsed by this class implementation
     * @throws WrongSubclassException
     * @throws IOException
     * @throws FailedReferenceException
     */
    public List<? extends Registration> fetchRegistration(RegistrationReference reference) throws IOException, ParseException, WrongSubclassException, DataStreamException, FailedReferenceException {
        this.getLog().info("Fetching registration from reference "+reference.getURI());
        if (!this.managedRegistrationReferenceClass.isInstance(reference)) {
            throw new WrongSubclassException(this.managedRegistrationReferenceClass, reference);
        }
        InputStream registrationData;
        URI uri = this.getRegistrationInterface(reference);
        try {
            registrationData = this.getRegistrationFetcher().fetch(uri);
        } catch (HttpStatusException e) {
            throw new FailedReferenceException(reference, e);
        }
        return this.parseRegistration(
            registrationData
        );
    }




    /** Checksum fetching **/

    /**
     * @return a URL to call for fetching the checksum map
     */
    protected abstract URI getListChecksumInterface(OffsetDateTime fromDate);

    /**
     * Parse the response contents into Checksum instances
     * Must close the responseContent InputStream when done parsing
     * @param responseContent
     * @return Stream of Checksum instances for further processing
     */
    protected abstract ItemInputStream<? extends EntityReference> parseChecksumResponse(InputStream responseContent) throws DataFordelerException;

    /**
     * Fetches checksum data (for synchronization) from the register. Plugins are free to implement their own version
     * @param fromDate
     * @return
     * @throws DataFordelerException
     */
    public ItemInputStream<? extends EntityReference> listRegisterChecksums(OffsetDateTime fromDate) throws DataFordelerException {
        this.getLog().info("Listing register checksums (" + (fromDate == null ? "ALL" : "since "+fromDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)) + ")");
        URI checksumInterface = this.getListChecksumInterface(fromDate);
        InputStream responseBody = this.getRegisterManager().getChecksumFetcher().fetch(checksumInterface);
        return this.parseChecksumResponse(responseBody);
    }

    public List<? extends EntityReference> listLocalChecksums(OffsetDateTime fromDate) throws DataFordelerException {
        // Look in the local database for checksums
        // TODO: Should we return a Map instead, for quicker lookup?
        return null;
    }




    /**
     * Utility method to be used by subclasses
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, a custom path, and no query or fragment
     * @throws URISyntaxException
     */
    public static URI expandBaseURI(URI base, String path) {
        return RegisterManager.expandBaseURI(base, path);
    }
    /**
     * Utility method to be used by subclasses
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, and a custom path query and fragment
     * @throws URISyntaxException
     */
    public static URI expandBaseURI(URI base, String path, String query, String fragment) {
        return RegisterManager.expandBaseURI(base, path, query, fragment);
    }

    protected abstract Logger getLog();
}
