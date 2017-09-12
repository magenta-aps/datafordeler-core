package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.util.HttpGetWithEntity;
import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Scanner;
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

    private byte[] delimiter = new byte[]{'\n'};

    private ObjectMapper objectMapper = new ObjectMapper();

    private String scrollIdJsonKey = "scroll_id";

    private int throttle = 1000;

    public void setThrottle(int throttleMillis) {
        this.throttle = throttleMillis;
    }

    public byte[] getDelimiter() {
        return this.delimiter;
    }

    public void setDelimiter(byte[] delimiter) {
        this.delimiter = delimiter;
    }

    private Pattern scrollIdPattern;

    private Logger log = LogManager.getLogger(ScanScrollCommunicator.class);

    public void setScrollIdJsonKey(String scrollIdJsonKey) {
        this.scrollIdJsonKey = scrollIdJsonKey;
        this.recompilePattern();
    }

    private void recompilePattern() {
        this.scrollIdPattern = Pattern.compile("\""+this.scrollIdJsonKey+"\":\\s*\"([a-zA-Z0-9=]+)\"");
    }

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
    public InputStream fetch(URI initialUri, URI scrollUri, final String body) throws HttpStatusException, DataStreamException {
        CloseableHttpClient httpclient = this.buildClient();

        try {
            final URI startUri = new URI(initialUri.getScheme(), initialUri.getUserInfo(), initialUri.getHost(), initialUri.getPort(), initialUri.getPath(), "search_type=scan&scroll=1m", null);

            PipedInputStream inputStream = new PipedInputStream(); // Return this one
            final BufferedOutputStream outputStream = new BufferedOutputStream(new PipedOutputStream(inputStream));

            log.info("Streams created");
            Thread fetcher = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JsonNode responseNode;

                        File postFile = new File("data/initial.json");

                        if (postFile.exists()) {

                            log.info("Getting data from cache");
                            InputStream postResponseData = new FileInputStream(postFile);
                            responseNode = objectMapper.readTree(postResponseData);
                            postResponseData.close();

                        } else {

                            HttpPost initialPost = new HttpPost(startUri);
                            initialPost.setEntity(new StringEntity(body, "utf-8"));
                            CloseableHttpResponse response;
                            log.info("Sending initial POST to " + startUri);
                            try {
                                response = httpclient.execute(initialPost);
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new DataStreamException(e);
                            }
                            log.info("Initial POST sent");
                            log.info("HTTP status: " + response.getStatusLine().getStatusCode());
                            if (response.getStatusLine().getStatusCode() != 200) {
                                log.info(response.getEntity().getContent());
                            }

                            responseNode = objectMapper.readTree(response.getEntity().getContent());

                            postFile.createNewFile();
                            FileWriter fw = new FileWriter(postFile);
                            fw.write(responseNode.toString());
                            fw.close();
                        }


                        String scrollId = responseNode.get(ScanScrollCommunicator.this.scrollIdJsonKey).asText();
                        int i = 0;
                        while (scrollId != null) {

                            InputStream getResponseData;

                            File getFile = new File("data/data"+i+".json");
                            if (getFile.exists()) {

                                getResponseData = new FileInputStream(getFile);


                            } else {

                                URI fetchUri = new URI(scrollUri.getScheme(), scrollUri.getUserInfo(), scrollUri.getHost(), scrollUri.getPort(), scrollUri.getPath(), "scroll=10m", null);
                                HttpGetWithEntity partialGet = new HttpGetWithEntity(fetchUri);
                                partialGet.setEntity(new StringEntity(scrollId));
                                try {
                                    log.info("Sending chunk GET to " + fetchUri);
                                    CloseableHttpResponse response = httpclient.execute(partialGet);
                                    getResponseData = new BufferedInputStream(response.getEntity().getContent(), 8192);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    throw new DataStreamException(e);
                                }
                            }

                            try {
                                int peekSize = 400;
                                getResponseData.mark(peekSize);
                                byte[] peekBytes = new byte[peekSize];
                                getResponseData.read(peekBytes, 0, peekSize);
                                getResponseData.reset();

                                String peekString = new String(peekBytes, 0, peekSize, "utf-8");
                                Matcher m = ScanScrollCommunicator.this.scrollIdPattern.matcher(peekString);
                                if (m.find()) {
                                    scrollId = m.group(1);
                                    log.info("found next scrollId");
                                } else {
                                    scrollId = null;
                                    log.info("next scrollId not found");
                                }
                                IOUtils.copy(getResponseData, outputStream);

                                postFile.createNewFile();
                                FileWriter fw = new FileWriter(getFile);
                                IOUtils.copy(getResponseData, fw);
                                fw.close();
                                i++;

                            } catch (IOException e) {
                                e.printStackTrace();
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
                                log.info("Closing outputstream");
                                outputStream.close();
                            }

                        }
                    } catch (DataStreamException | IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            });

            fetcher.start();

            return inputStream;


        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}