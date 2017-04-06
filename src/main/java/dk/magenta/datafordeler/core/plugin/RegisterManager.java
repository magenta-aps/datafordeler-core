package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.ItemInputStream;
import dk.magenta.datafordeler.core.event.Event;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by lars on 05-04-17.
 */
public abstract class RegisterManager {

    protected List<EntityManager> entityManagers;

    protected Map<String, EntityManager> entityManagerBySchema;

    protected Map<String, EntityManager> entityManagerByURISubstring;

    protected HashSet<String> handledSchemas;

    private TriggerKey pullTriggerKey;

    protected Class<? extends PullTask> pullTaskClass = PullTask.class;

    public RegisterManager() {
        this.handledSchemas = new HashSet<>();
        this.entityManagers = new ArrayList<>();
        this.entityManagerBySchema = new HashMap<>();
        this.entityManagerByURISubstring = new HashMap<>();
    }


    public abstract URI getBaseEndpoint();

    /**
     * Plugins must return a Fetcher instance from this method
     * @return
     */
    protected abstract Fetcher getEventFetcher();

    /**
     * Plugins must return an autowired ObjectMapper instance from this method
     * @return
     */
    protected abstract ObjectMapper getObjectMapper();

    public boolean handlesSchema(String schema) {
        return this.handledSchemas.contains(schema);
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

    public void addEntityManager(EntityManager entityManager, String schema) {
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
        InputStream responseBody = this.getEventFetcher().fetch(this.getEventInterface());
        ItemInputStream<Event> eventStream = this.parseEventResponse(responseBody);
        return eventStream;
    }

    protected ItemInputStream<Event> parseEventResponse(InputStream responseContent) throws DataFordelerException {
        return ItemInputStream.parseJsonStream(responseContent, Event.class, "events", this.getObjectMapper());
    }

    public void setupPullSchedule(String cronSchedule) {
        this.setupPullSchedule(cronSchedule, false);
    }

    public void setupPullSchedule(String cronSchedule, boolean dummyRun) {
        //String cronSchedule = this.getPullCronSchedule();
        Scheduler scheduler;

        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            if (cronSchedule != null) {
                this.pullTriggerKey = TriggerKey.triggerKey("pullTrigger", this.getClass().getName());
                // Set up new schedule, or replace existing
                Trigger pullTrigger = TriggerBuilder.newTrigger()
                        .withIdentity(this.pullTriggerKey)
                        .withSchedule(
                                CronScheduleBuilder.cronSchedule(cronSchedule)
                        ).build();
                this.pullTriggerKey = pullTrigger.getKey();

                JobDataMap jobData = new JobDataMap();
                jobData.put(PullTask.DATA_REGISTERMANAGER, this);
                jobData.put(PullTask.DATA_DUMMYRUN, dummyRun);
                JobDetail job = JobBuilder.newJob(this.pullTaskClass)
                        .withIdentity("pullTask")
                        .setJobData(jobData)
                        .build();

                scheduler.scheduleJob(job, Collections.singleton(pullTrigger), true);
                scheduler.start();
            } else {
                // Remove old schedule
                if (this.pullTriggerKey != null) {
                    scheduler.unscheduleJob(this.pullTriggerKey);
                }
            }

        } catch (SchedulerException e) {

        }
    }

    /**
     * Return a Cron expression telling when to perform a pull from the register
     * @return
     */
    public String getPullCronSchedule() {
        return null;
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
        return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), (base.getPath() == null ? "" : base.getPath()) + path, query, fragment);
    }
}
