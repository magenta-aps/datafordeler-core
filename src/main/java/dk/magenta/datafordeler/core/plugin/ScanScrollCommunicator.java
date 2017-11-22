package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.util.HttpGetWithEntity;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lars on 27-06-17.
 * A special Communicator that fetches data over a HTTP connection by the 
 * scan-scroll pattern: We specify the query in a POST, then get a handle back 
 * that we can use in a series of subsequent GET requests to get all the data 
 * (which tends to be a lot).
 */
public class ScanScrollCommunicator extends HttpCommunicator {

    public ScanScrollCommunicator() {
        this.recompilePattern();
    }

    public ScanScrollCommunicator(String username, String password) {
        super(username, password);
        this.recompilePattern();
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    private String scrollIdJsonKey = "scroll_id";

    public void setScrollIdJsonKey(String scrollIdJsonKey) {
        this.scrollIdJsonKey = scrollIdJsonKey;
        this.recompilePattern();
    }

    private Pattern scrollIdPattern;

    private void recompilePattern() {
        this.scrollIdPattern = Pattern.compile("\""+this.scrollIdJsonKey+"\":\\s*\"([a-zA-Z0-9=]+)\"");
    }

    public static char delimiter = '\n';


    private int throttle = 1000;

    public void setThrottle(int throttleMillis) {
        this.throttle = throttleMillis;
    }

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;

    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    private Logger log = LogManager.getLogger(ScanScrollCommunicator.class);


    private Pattern emptyResultsPattern = Pattern.compile("\"hits\":\\s*\\[\\s*\\]");

    private HashMap<InputStream, Thread> fetches = new HashMap();

    /**
     * Fetch data from the external source; sends a POST to the initialUri, 
     * with the body, and waits for a response.
     * If all goes well, this response contains a scrollId somewhere in the 
     * JSON, which is the handle we use on subsequent requests.
     * For the purposes of this project, we assume the response is JSON-encoded.
     * We then send further requests using the handle, expecting a handle in 
     * each response until we are done. The full payload of all GET responses 
     * is sent into the InputStream that we return.
     * This all happens in a thread, so you should get an InputStream returned immediately.
     */
    public InputStream fetch(URI initialUri, URI scrollUri, final String body) throws HttpStatusException, DataStreamException, URISyntaxException, IOException {
        CloseableHttpClient httpclient = this.buildClient();

        final URI startUri = new URI(initialUri.getScheme(), initialUri.getUserInfo(), initialUri.getHost(), initialUri.getPort(), initialUri.getPath(), "search_type=scan&scroll=1m", null);

        PipedInputStream inputStream = new PipedInputStream(); // Return this one
        final BufferedOutputStream outputStream = new BufferedOutputStream(new PipedOutputStream(inputStream));

        log.info("Streams created");
        Thread fetcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JsonNode responseNode;

                    HttpPost initialPost = new HttpPost(startUri);
                    initialPost.setEntity(new StringEntity(body, "utf-8"));
                    CloseableHttpResponse response;
                    log.info("Sending initial POST to " + startUri);
                    try {
                        response = httpclient.execute(initialPost);
                    } catch (IOException e) {
                        log.error(e);
                        throw new DataStreamException(e);
                    }
                    log.info("Initial POST sent");
                    log.info("HTTP status: " + response.getStatusLine().getStatusCode());
                    InputStream content = response.getEntity().getContent();
                    if (response.getStatusLine().getStatusCode() != 200) {
                        log.error(InputStreamReader.readInputStream(response.getEntity().getContent()));
                        throw new HttpStatusException(response.getStatusLine(), startUri);
                    }

                    /*
                    String postResponseContent = InputStreamReader.readInputStream(content);
                    outputStream.write(postResponseContent.getBytes("UTF-8"));
                    outputStream.flush();
                    responseNode = objectMapper.readTree(postResponseContent);
                    */

                    responseNode = objectMapper.readTree(content);

                    String scrollId = responseNode.get(ScanScrollCommunicator.this.scrollIdJsonKey).asText();
                    while (scrollId != null) {
                        InputStream getResponseData;

                        URI fetchUri = new URI(scrollUri.getScheme(), scrollUri.getUserInfo(), scrollUri.getHost(), scrollUri.getPort(), scrollUri.getPath(), "scroll=10m", null);
                        HttpGetWithEntity partialGet = new HttpGetWithEntity(fetchUri);
                        partialGet.setEntity(new StringEntity(scrollId));
                        try {
                            log.info("Sending chunk GET to " + fetchUri);
                            response = httpclient.execute(partialGet);
                            getResponseData = new BufferedInputStream(response.getEntity().getContent(), 8192);


                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new DataStreamException(e);
                        }

                        try {
                            int peekSize = 1000;
                            getResponseData.mark(peekSize);
                            byte[] peekBytes = new byte[peekSize];
                            getResponseData.read(peekBytes, 0, peekSize);
                            getResponseData.reset();

                            String peekString = new String(peekBytes, 0, peekSize, "utf-8");

                            Matcher m = ScanScrollCommunicator.this.emptyResultsPattern.matcher(peekString);
                            if (m.find()) {
                                log.info("Empty results encountered");
                                scrollId = null;
                            } else {
                                m = ScanScrollCommunicator.this.scrollIdPattern.matcher(peekString);
                                if (m.find()) {
                                    scrollId = m.group(1);
                                    log.info("found next scrollId");
                                } else {
                                    scrollId = null;
                                    log.info("next scrollId not found");
                                }
                                IOUtils.copy(getResponseData, outputStream);
                            }

                        } catch (IOException e) {
                            throw new DataStreamException(e);
                        } finally {
                            getResponseData.close();
                        }

                        if (scrollId != null) {
                            // There is more data
                            outputStream.write(delimiter);
                            if (throttle > 0) {
                                try {
                                    log.info("Waiting "+throttle+" milliseconds before next request");
                                    Thread.sleep(throttle);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            // Reached the end
                            break;
                        }

                    }
                } catch (DataStreamException | IOException | URISyntaxException | HttpStatusException e) {
                    ScanScrollCommunicator.this.log.error(e);
                    throw new RuntimeException(e);
                } finally {
                    try {
                        log.info("Closing outputstream");
                        outputStream.close();
                    } catch (IOException e1) {
                    }
                    ScanScrollCommunicator.this.fetches.remove(inputStream);
                }
            }
        });

        if (this.uncaughtExceptionHandler != null) {
            fetcher.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        }
        this.fetches.put(inputStream, fetcher);

        fetcher.start();
        return inputStream;
    }

    public void wait(InputStream inputStream) {
        Thread fetcher = this.fetches.get(inputStream);
        if (fetcher != null) {
            try {
                fetcher.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}