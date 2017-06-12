package dk.magenta.datafordeler.plugindemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.core.database.EntityReference;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.Event;
import dk.magenta.datafordeler.core.plugin.*;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Created by lars on 05-04-17.
 */
@Component
public class DemoRegisterManager extends RegisterManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DemoConfigurationManager configurationManager;

    @Autowired
    private DemoPlugin plugin;

    private HttpCommunicator commonFetcher;

    protected Logger log = LogManager.getLogger("DemoRegisterManager");

    public DemoRegisterManager() {
        this.commonFetcher = new HttpCommunicator();
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public URI getBaseEndpoint() {
        try {
            return new URI("http", null, "localhost", TestConfig.servicePort, "/test", null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Communicator getEventFetcher() {
        return this.commonFetcher;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /** Event fetching **/

    @Override
    protected URI getEventInterface() {
        return expandBaseURI(this.getBaseEndpoint(), "/getNewEvents");
    }

    @Override
    protected ItemInputStream<Event> parseEventResponse(InputStream responseContent) throws DataFordelerException {
        if (this.log.isDebugEnabled()) {
            responseContent = this.printStream(responseContent); // Just for printing, can be omitted
        }
        return super.parseEventResponse(responseContent);
    }

    public String getPullCronSchedule() {
        return this.configurationManager.getConfiguration().getPullCronSchedule();
    }

    /* listChecksums */

    @Override
    protected Communicator getChecksumFetcher() {
        return this.commonFetcher;
    }

    public URI getListChecksumInterface(String schema, OffsetDateTime from) {
        ListHashMap<String, String> parameters = new ListHashMap<>();
        if (schema != null) {
            parameters.add("objectType", schema);
        }
        if (from != null) {
            parameters.add("timestamp", from.format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return expandBaseURI(this.getBaseEndpoint(), "/listChecksums", RegisterManager.joinQueryString(parameters), null);
    }

    @Override
    protected ItemInputStream<EntityReference> parseChecksumResponse(InputStream responseContent) throws DataFordelerException {
        //responseContent = this.printStream(responseContent); // Just for printing, can be omitted
        HashMap<String, Class<? extends EntityReference>> classMap = new HashMap<>();
        for (EntityManager entityManager : this.entityManagers) {
            classMap.put(entityManager.getSchema(), entityManager.getManagedEntityReferenceClass());
        }
        return ItemInputStream.parseJsonStream(responseContent, classMap, "items", "type", this.objectMapper);
    }


    private InputStream printStream(InputStream input) {
        try {
            if (input.markSupported()) {
                input.mark(8192);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(input, baos);
            byte[] bytes = baos.toByteArray();
            this.log.debug(new String(bytes, "utf-8"));
            if (input.markSupported()) {
                input.reset();
                return input;
            }
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }
}
