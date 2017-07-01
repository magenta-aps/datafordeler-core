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

    public void setThrottle(int throttle) {
        this.throttle = throttle;
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
                        HttpPost initialPost = new HttpPost(startUri);
                        initialPost.setEntity(new StringEntity(body, "utf-8"));
                        CloseableHttpResponse response;
                        log.info("Sending initial post");
                        try {
                            response = httpclient.execute(initialPost);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new DataStreamException(e);
                        }
                        log.info("Initial post sent");

                        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
                        String scrollId = responseNode.get("_scroll_id").asText();

                        while (scrollId != null) {
                            log.info("scrollId: "+scrollId);
                            URI fetchUri = new URI(scrollUri.getScheme(), scrollUri.getUserInfo(), scrollUri.getHost(), scrollUri.getPort(), scrollUri.getPath(), "scroll=1m", null);
                            HttpGetWithEntity partialGet = new HttpGetWithEntity(fetchUri);
                            partialGet.setEntity(new StringEntity(scrollId));
                            try {
                                response = httpclient.execute(partialGet);
                                BufferedInputStream data = new BufferedInputStream(response.getEntity().getContent(), 8192);

                                int peekSize = 400;
                                data.mark(peekSize);
                                byte[] peekBytes = new byte[peekSize];
                                data.read(peekBytes, 0, peekSize);
                                data.reset();

                                String peekString = new String(peekBytes, 0, peekSize, "utf-8");
                                Matcher m = ScanScrollCommunicator.this.scrollIdPattern.matcher(peekString);
                                if (m.find()) {
                                    scrollId = m.group(1);
                                    log.info("found next scrollId");
                                } else {
                                    scrollId = null;
                                    log.info("next scrollId not found");
                                }
                                IOUtils.copy(data, outputStream);
                                if (scrollId != null) {
                                    // There is more data
                                    outputStream.write(delimiter);
                                } else {
                                    // Reached the end
                                    outputStream.close();
                                }
                                if (throttle > 0) {
                                    try {
                                        Thread.sleep(throttle);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new DataStreamException(e);
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