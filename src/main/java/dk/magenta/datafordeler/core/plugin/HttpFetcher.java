package dk.magenta.datafordeler.core.plugin;


import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by lars on 13-03-17.
 */
public class HttpFetcher implements Fetcher {

    @Override
    public InputStream fetch(URI uri) throws HttpStatusException, DataStreamException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(get);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() != 200) {
            System.out.println("uri: "+uri);
            throw new HttpStatusException(statusLine, uri);
        }
        try {
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }
}
