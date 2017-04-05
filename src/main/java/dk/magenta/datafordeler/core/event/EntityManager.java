package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Checksum;
import dk.magenta.datafordeler.core.ItemInputStream;
import dk.magenta.datafordeler.core.Receipt;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.FailedReferenceException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.exception.WrongSubclassException;
import dk.magenta.datafordeler.core.model.Entity;
import dk.magenta.datafordeler.core.model.EntityReference;
import dk.magenta.datafordeler.core.model.RegistrationReference;
import dk.magenta.datafordeler.core.model.Registration;
import dk.magenta.datafordeler.core.plugin.Fetcher;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 13-03-17.
 *
 * Entity (and associates) specific manager. Subclass in plugins
 * A plugin can have any number of Entity classes, each needing their own way of handling
 */
public abstract class EntityManager {
    protected Class<? extends Entity> managedEntityClass;
    protected Class<? extends RegistrationReference> managedRegistrationReferenceClass;
    protected Class<? extends Registration> managedRegistrationClass;
    protected Fetcher registrationFetcher;

    public Class<? extends Entity> getManagedEntityClass() {
        return this.managedEntityClass;
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



    public abstract URI getBaseEndpoint();








    /** Receipt sending **/

    /**
     * @return A URL to send the given receipt to
     * Depending on the register, the URL could change between receipts (such as the objectID being part of it)
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
        CloseableHttpClient httpclient = HttpClients.createDefault();
        URI receiptEndpoint = this.getReceiptEndpoint(receipt);
        String payload = objectMapper.writeValueAsString(receipt);
        System.out.println("Sending receipt to " + receiptEndpoint);
        System.out.println(payload);
        HttpPost post = new HttpPost(receiptEndpoint);
        post.setEntity(new StringEntity(payload));
        // TODO: Do this in a thread?
        CloseableHttpResponse response = httpclient.execute(post);
        return response.getStatusLine().getStatusCode();
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
    public abstract Registration parseRegistration(InputStream registrationData) throws IOException;

    /**
     * Parse incoming data into a Registration (data coming from within a request envelope)
     * @param registrationData
     * @return
     * @throws IOException
     */
    public abstract Registration parseRegistration(String registrationData, String charsetName) throws IOException;



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
    public Registration fetchRegistration(RegistrationReference reference) throws WrongSubclassException, IOException, FailedReferenceException {
        if (!this.managedRegistrationReferenceClass.isInstance(reference)) {
            throw new WrongSubclassException(this.managedRegistrationReferenceClass, reference);
        }
        InputStream registrationData = null;
        URI uri = this.getRegistrationInterface(reference);
        try {
            registrationData = this.registrationFetcher.fetch(uri);
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
    public ItemInputStream<? extends EntityReference> listRegisterChecksums(OffsetDateTime fromDate) throws DataFordelerException, IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(this.getListChecksumInterface(fromDate));
        System.out.println(this.getListChecksumInterface(fromDate));
        // TODO: Do this in a thread?
        CloseableHttpResponse response = httpclient.execute(get);
        InputStream responseBody = response.getEntity().getContent();
        return this.parseChecksumResponse(responseBody);
    }

    public List<Checksum> listLocalChecksums(OffsetDateTime fromDate) throws DataFordelerException {
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
    public static URI expandBaseURI(URI base, String path) throws URISyntaxException {
        return expandBaseURI(base, path, null, null);
    }
    /**
     * Utility method to be used by subclasses
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, and a custom path query and fragment
     * @throws URISyntaxException
     */
    public static URI expandBaseURI(URI base, String path, String query, String fragment) throws URISyntaxException {
        return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), (base.getPath() == null ? "" : base.getPath()) + path, query, fragment);
    }
}
