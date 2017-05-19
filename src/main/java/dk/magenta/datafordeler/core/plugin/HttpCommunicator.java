package dk.magenta.datafordeler.core.plugin;


import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * Created by lars on 13-03-17.
 */
public class HttpCommunicator implements Communicator {

    private CookieStore cookieStore;

    public HttpCommunicator() {
        this.cookieStore = new BasicCookieStore();
    }

    @Override
    public InputStream fetch(URI uri) throws HttpStatusException, DataStreamException {
        CloseableHttpClient httpclient = this.buildClient();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response;
        try {
            response = httpclient.execute(get);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() != 200) {
            throw new HttpStatusException(statusLine, uri);
        }

        try {
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    @Override
    public StatusLine send(URI endpoint, String payload) throws IOException {
        CloseableHttpClient httpclient = this.buildClient();
        HttpPost request = new HttpPost(endpoint);
        try {
            request.setEntity(new StringEntity(payload));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // TODO: Do this in a thread?

        CloseableHttpResponse response = httpclient.execute(request);
        return response.getStatusLine();
    }

    private CloseableHttpClient buildClient() {
        return HttpClients.custom().setDefaultCookieStore(this.cookieStore).build();
    }

}
