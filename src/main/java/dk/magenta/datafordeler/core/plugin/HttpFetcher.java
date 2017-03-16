package dk.magenta.datafordeler.core.plugin;


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
    public InputStream fetch(URI uri) throws IOException, HttpStatusException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = httpclient.execute(get);
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() != 200) {
            throw new HttpStatusException(statusLine);
        }
        return response.getEntity().getContent();
    }
}
