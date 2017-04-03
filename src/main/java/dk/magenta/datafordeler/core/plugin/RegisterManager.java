package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Checksum;
import dk.magenta.datafordeler.core.ChecksumInputStream;
import dk.magenta.datafordeler.core.Receipt;
import dk.magenta.datafordeler.core.oldevent.BusinessEvent;
import dk.magenta.datafordeler.core.oldevent.DataEvent;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.event.Reference;
import dk.magenta.datafordeler.core.model.Registration;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Created by lars on 11-01-17.
*/
public abstract class RegisterManager {

    public abstract URI getBaseEndpoint();

    protected ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    /**
     * @return A URL to send the given receipt to
     * Depending on the register, the URL could change between receipts (such as the objectID being part of it)
     */
    protected abstract URI getReceiptEndpoint(Receipt receipt);

    public int sendReceipt(Receipt receipt) throws IOException {
        ObjectMapper objectMapper = this.getObjectMapper();
        // Send receipts to the register interface at the address pointed to by getReceiptEndpoint()
        CloseableHttpClient httpclient = HttpClients.createDefault();

        URI receiptEndpoint = this.getReceiptEndpoint(receipt);
        System.out.println("Sending receipt to " + receiptEndpoint);
        HttpPost post = new HttpPost(receiptEndpoint);
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(receipt)));
        // TODO: Do this in a thread?
        CloseableHttpResponse response = httpclient.execute(post);
        return response.getStatusLine().getStatusCode();
    }


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

    public ChecksumInputStream listRegisterChecksums(Date fromDate) throws DataFordelerException {
        // Request checksums from the register pointed to by getChecksumInterface()
        // Put the response through parseChecksumResponse
        return null;
    }






    /**
     * @return a URL to call for fetching registrations
     * Depending on the register, the URL could change between registrations (such as the checksum being part of it)
     */
    /*protected abstract URL getRegistrationsInterface(Reference reference);

    public List<Registration> getRegistrations(Collection<Reference> references) throws DataFordelerException {
        // Request registrations from the register pointed to by getRegistrationsInterface()
        // TODO: Should this return a stream of instances?
        return null;
    }

    public Registration getRegistration(Reference reference) throws DataFordelerException {
        // Request registrations from the register pointed to by getRegistrationsInterface()
        // TODO: Should this return a stream of instances?
        return null;
    }*/






    public List<Checksum> listLocalChecksums(Date fromDate) throws DataFordelerException {
        // Look in the local database for checksums
        // TODO: Should we return a Map instead, for quicker lookup?
        return null;
    }





/*
    protected abstract void processBusinessEvent(BusinessEvent event);

    protected abstract void processDataEvent(DataEvent event);
*/
    public final void synchronize() {
        this.synchronize(null);
    }

    public void synchronize(Date fromdate) {
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
