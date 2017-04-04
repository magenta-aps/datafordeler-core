package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Checksum;
import dk.magenta.datafordeler.core.ItemInputStream;
import dk.magenta.datafordeler.core.Receipt;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.model.EntityReference;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 11-01-17.
*/
public abstract class RegisterManager {

    /**
     * Plugins must return an autowired ObjectMapper instance from this method
     * @return
     */
    protected abstract ObjectMapper getObjectMapper();



    public abstract URI getBaseEndpoint();


    // RECEIPTS

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




    // LISTCHECKSUMS

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













    public final void synchronize() {
        this.synchronize(null);
    }

    public void synchronize(OffsetDateTime fromdate) {
        // TODO: Likely move this to the dk.magenta.datafordeler.engine.Engine class
        // Fetch list of checksums with listRegisterChecksums
        // Find missing/mismatching versions
        // Fetch these versions
        // Update DB
        // Send receipts
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
        return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), path, query, fragment);
    }

}
