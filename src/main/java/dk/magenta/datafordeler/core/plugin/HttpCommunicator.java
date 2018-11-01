package dk.magenta.datafordeler.core.plugin;


import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

/**
 * Very basic Communicator that fetches a resource over HTTP,
 * and/or sends a payload on a POST request. Takes optional 
 * username and password for authentication, and holds cookies
 * between requests.
 */
public class HttpCommunicator implements Communicator {

    private CookieStore cookieStore;

    private String username;
    private String password;

    public HttpCommunicator() {
        this.cookieStore = new BasicCookieStore();
    }

    public HttpCommunicator(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    @Override
    public InputStream fetch(URI uri) throws HttpStatusException, DataStreamException {
        return this.get(uri, null);
    }

    public InputStream get(URI uri, Map<String, String> headers) throws DataStreamException, HttpStatusException {
        CloseableHttpClient httpclient = this.buildClient();
        HttpGet get = new HttpGet(uri);
        if (headers != null) {
            for (String key : headers.keySet()) {
                get.setHeader(key, headers.get(key));
            }
        }
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

    public InputStream post(URI uri, Map<String, String> parameters, Map<String, String> headers) throws DataStreamException, HttpStatusException, UnsupportedEncodingException {
        ArrayList<NameValuePair> paramList = new ArrayList<>();
        for (String key : parameters.keySet()) {
            paramList.add(new BasicNameValuePair(key, parameters.get(key)));
        }
        return this.post(
                uri,
                new UrlEncodedFormEntity(paramList),
                headers
        );
    }

    public InputStream post(URI uri, HttpEntity body, Map<String, String> headers) throws DataStreamException, HttpStatusException {
        CloseableHttpClient httpclient = this.buildClient();
        HttpPost post = new HttpPost(uri);
        if (headers != null) {
            for (String key : headers.keySet()) {
                post.setHeader(key, headers.get(key));
            }
        }
        post.setEntity(body);
        try {
            HttpResponse response = httpclient.execute(post);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != 200) {
                throw new HttpStatusException(statusLine, uri);
            }
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

    protected CloseableHttpClient buildClient() {
        HttpClientBuilder builder = HttpClients.custom();
        if (this.username != null && this.password != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.username, this.password));
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
        builder.setDefaultCookieStore(this.cookieStore);
        return builder.build();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
