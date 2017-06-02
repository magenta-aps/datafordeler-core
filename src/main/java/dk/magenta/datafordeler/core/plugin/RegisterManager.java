package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.core.io.Event;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.EntityReference;
import dk.magenta.datafordeler.core.util.ListHashMap;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.UriUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by lars on 05-04-17.
 */
public abstract class RegisterManager {

    protected List<EntityManager> entityManagers;

    protected Map<String, EntityManager> entityManagerBySchema;

    protected Map<String, EntityManager> entityManagerByURISubstring;

    protected HashSet<String> handledSchemas;

    public RegisterManager() {
        this.handledSchemas = new HashSet<>();
        this.entityManagers = new ArrayList<>();
        this.entityManagerBySchema = new HashMap<>();
        this.entityManagerByURISubstring = new HashMap<>();
    }

    protected abstract Logger getLog();

    public abstract Plugin getPlugin();


    public abstract URI getBaseEndpoint();

    /**
     * Plugins must return a Fetcher instance from this method
     * @return
     */
    protected abstract Communicator getEventFetcher();

    /**
     * Plugins must return an autowired ObjectMapper instance from this method
     * @return
     */
    protected abstract ObjectMapper getObjectMapper();

    public boolean handlesSchema(String schema) {
        return this.handledSchemas.contains(schema);
    }

    public List<EntityManager> getEntityManagers() {
        return this.entityManagers;
    }

    public EntityManager getEntityManager(String schema) {
        return this.entityManagerBySchema.get(schema);
    }

    public EntityManager getEntityManager(URI uri) {
        String uriString = uri.toString();
        for (String substring : this.entityManagerByURISubstring.keySet()) {
            if (uriString.startsWith(substring)) {
                return this.entityManagerByURISubstring.get(substring);
            }
        }
        return null;
    }

    public EntityManager getEntityManager(Class<? extends Entity> entityClass) {
        for (EntityManager entityManager : this.entityManagers) {
            if (entityManager.getManagedEntityClass() == entityClass) {
                return entityManager;
            }
        }
        return null;
    }

    public void addEntityManager(EntityManager entityManager) {
        entityManager.setRegisterManager(this);
        String schema = entityManager.getSchema();
        this.handledSchemas.add(schema);
        this.entityManagers.add(entityManager);
        this.entityManagerBySchema.put(schema, entityManager);
        for (String substring : entityManager.getHandledURISubstrings()) {
            this.entityManagerByURISubstring.put(substring, entityManager);
        }
    }

    public Collection<String> getHandledURISubstrings() {
        return this.entityManagerByURISubstring.keySet();
    }




    /** Event fetching **/

    protected abstract URI getEventInterface();

    public ItemInputStream<Event> pullEvents() throws DataFordelerException {
        URI eventInterface = this.getEventInterface();
        this.getLog().info("Pulling events from "+eventInterface);
        Communicator eventCommunicator = this.getEventFetcher();
        InputStream responseBody = eventCommunicator.fetch(eventInterface);
        return this.parseEventResponse(responseBody);
    }

    protected ItemInputStream<Event> parseEventResponse(InputStream responseContent) throws DataFordelerException {
        return ItemInputStream.parseJsonStream(responseContent, Event.class, "events", this.getObjectMapper());
    }

    /**
     * Return a Cron expression telling when to perform a pull from the register
     * Subclasses that want to enable pull must override this and return a valid cron expression
     * @return
     */
    public String getPullCronSchedule() {
        return null;
    }

    /** Checksum fetching **/

    /**
     * Plugins must return a Fetcher instance from this method
     * @return
     */
    protected abstract Communicator getChecksumFetcher();

    public abstract URI getListChecksumInterface(String schema, OffsetDateTime from);

    /**
     * Fetches checksum data (for synchronization) from the register. Plugins are free to implement their own version
     * @param fromDate
     * @return
     * @throws DataFordelerException
     */
    public ItemInputStream<? extends EntityReference> listRegisterChecksums(String schema, OffsetDateTime fromDate) throws DataFordelerException {
        URI checksumInterface = this.getListChecksumInterface(schema, fromDate);
        this.getLog().info(
                "Getting " + (fromDate==null ? "all " : "") + "checksums for " +
                (schema==null ? "all schemas":("schema "+schema)) +
                (fromDate!=null ? (" since "+fromDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)):"") +
                " from address "+checksumInterface);
        // TODO: Do this in a thread?
        InputStream responseBody = this.getChecksumFetcher().fetch(checksumInterface);
        if (schema != null) {
            EntityManager entityManager = this.getEntityManager(schema);
            if (entityManager != null) {
                return entityManager.parseChecksumResponse(responseBody);
            }
        }
        return this.parseChecksumResponse(responseBody);
    }

    protected ItemInputStream<? extends EntityReference> parseChecksumResponse(InputStream responseContent) throws DataFordelerException {
        HashMap<String, Class<? extends EntityReference>> classMap = new HashMap<>();
        for (EntityManager entityManager : this.entityManagers) {
            classMap.put(entityManager.getSchema(), entityManager.getManagedEntityReferenceClass());
        }
        return ItemInputStream.parseJsonStream(responseContent, classMap, "items", "type", this.getObjectMapper());
    }






    /**
     * Utility method to be used by subclasses
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, a custom path, and no query or fragment
     * @throws URISyntaxException
     */
    public static URI expandBaseURI(URI base, String path) {
        return expandBaseURI(base, path, null, null);
    }
    /**
     * Utility method to be used by subclasses
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, and a custom path query and fragment
     * @throws URISyntaxException
     */
    public static URI expandBaseURI(URI base, String path, String query, String fragment) {
        try {
            return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(),base.getPath() + path, query, fragment);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String joinQueryString(ListHashMap<String, String> params)  {
        StringJoiner sj = new StringJoiner("&");
        for (String key : params.keySet()) {
            ArrayList<String> values = params.get(key);
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    try {
                        sj.add(key+"="+ UriUtils.encodeQueryParam(value, "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return sj.toString();

    }
}