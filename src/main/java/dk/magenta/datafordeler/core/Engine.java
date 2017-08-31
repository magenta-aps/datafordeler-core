package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.io.Event;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class Engine {

    @Autowired
    PluginManager pluginManager;

    @Autowired
    QueryManager queryManager;

    @Autowired
    SessionManager sessionManager;

    Logger log = LogManager.getLogger("Engine");

    /**
     * Run bean initialization
     *
     */
    @PostConstruct
    public void init() {
        List<Plugin> plugins = this.pluginManager.getPlugins();
        if (plugins.isEmpty()) {
            this.log.info("Registered NO plugins");
        } else {
            for (Plugin plugin : plugins) {
                this.log.info("Registered plugin " + plugin.getClass().getCanonicalName());
                String schedule = plugin.getRegisterManager().getPullCronSchedule();
                if (schedule != null) {
                    this.log.info("    Has CRON schedule " + schedule);
                }
            /*Session session = this.sessionManager.getSessionFactory().openSession();
            try {
                this.synchronize(session, plugin, null);
            } catch (DataFordelerException e) {
                e.printStackTrace();
            }*/

            }
            for (Plugin plugin : plugins) {
                String schedule = plugin.getRegisterManager().getPullCronSchedule();
                if (schedule != null) {
                    this.setupPullSchedule(plugin.getRegisterManager());
                }
            }
        }
    }

    /*public void initialSynchronize() {
        System.out.println("initialSynchronize");
        for (Plugin plugin : this.pluginManager.getPlugins()) {
            if (!plugin.isDemo()) {
                Session session = this.sessionManager.getSessionFactory().openSession();
                try {
                    this.synchronize(session, plugin, null);
                } catch (DataFordelerException e) {
                    e.printStackTrace();
                }
                session.close();
            }
        }
    }*/


    /** Push **/

    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> boolean handleEvent(PluginSourceData event) {
        return this.handleEvent(event, null);
    }

    /**
     * Entry point for incoming events from the GAPI.
     * A registrationreference in the incoming event will be parsed, and the corresponding registration fetched
     * When a registration is at hand, it is saved and a receipt is sent to the entitymanager that handles the registration
     * @param event Event to be handled
     * */
    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> boolean handleEvent(PluginSourceData event, Plugin plugin) {
        log.info("Handling event '" + event.getId() + "'");
        OffsetDateTime eventReceived = OffsetDateTime.now();
        Receipt receipt;
        EntityManager entityManager = null;
        boolean success;
        Session session = null;
        try {
            List<? extends Registration> registrations;

            if (event.getData() == null) {
                String referenceData = event.getReference();
                if (referenceData == null) {
                    throw new MissingReferenceException(event);
                }

                log.info("Event contains a reference to a registration");
                // If the event is only a reference, request the actual object through the plugin
                URI referenceURI;
                try {
                    referenceURI = new URI(referenceData);
                } catch (URISyntaxException e) {
                    throw new InvalidReferenceException(referenceData);
                }
                if (plugin == null) {
                    plugin = pluginManager.getPluginForURI(referenceURI);
                }
                if (plugin == null) {
                    throw new PluginNotFoundException(referenceURI);
                }
                log.info("Handled by plugin " + plugin.getClass().getCanonicalName());
                entityManager = plugin.getEntityManager(referenceURI);
                if (entityManager == null) {
                    throw new EntityManagerNotFoundException(referenceURI);
                }

                try {
                    RegistrationReference reference = entityManager.parseReference(referenceURI);
                    log.info("Parsed reference: " + entityManager.getRegistrationInterface(reference));
                    registrations = entityManager.fetchRegistration(reference);
                    log.info("Referenced registration fetched");
                } catch (IOException e) {
                    throw new DataStreamException(e);
                }
            } else {
                log.info("Event contains a full registration");
                String schema = event.getSchema();
                if (plugin == null) {
                    plugin = pluginManager.getPluginForSchema(schema);
                }
                if (plugin == null) {
                    throw new PluginNotFoundException(schema, true);
                }
                log.info("Handled by plugin " + plugin.getClass().getCanonicalName());
                entityManager = plugin.getEntityManager(schema);
                if (entityManager == null) {
                    throw new EntityManagerNotFoundException(schema);
                }
                registrations = entityManager.parseRegistration(event.getData());
            }

            /*
            Session session = sessionManager.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            queryManager.saveRegistration(session, registration.getEntity(), registration);
            transaction.commit();
            session.close();
            */

            receipt = new Receipt(event.getId(), eventReceived);
            success = true;
        } catch (DataFordelerException e) {
            log.error("Error handling event", e);
            receipt = new Receipt(event.getId(), eventReceived, e);
            success = false;
            if (session != null) {
                session.close();
            }
        } catch (Exception e) {
            if (session != null) {
                session.close();
            }
            throw e;
        }
        if (entityManager == null) {
            log.error("No EntityManager found for event; cannot send receipt");
        } else {
            try {
               entityManager.sendReceipt(receipt);
            } catch (IOException e) {
                log.error("Failed sending receipt", e);
                e.printStackTrace();
            }
        }
        return success;
    }


    /** Pull **/

    private HashMap<String, TriggerKey> pullTriggerKeys = new HashMap<>();

    /**
     * Sets the schedule for the registerManager, based on the schedule defined in same
     * @param registerManager Registermanager to run pull jobs on
     */
    public void setupPullSchedule(RegisterManager registerManager) {
        this.setupPullSchedule(registerManager, registerManager.getPullCronSchedule(), false);
    }

    /**
     * Sets the schedule for the registerManager, given a cron string
     * @param registerManager Registermanager to run pull jobs on
     * @param cronSchedule A valid cron schedule, six items, space-separated
     * @param dummyRun For test purposes. If false, no pull will actually be run.
     */
    public void setupPullSchedule(RegisterManager registerManager, String cronSchedule, boolean dummyRun) {
        Scheduler scheduler;
        String registerManagerId = registerManager.getClass().getName() + registerManager.hashCode();

        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            if (cronSchedule != null) {
                this.log.info("Setting up cron with schedule " + cronSchedule + " to pull from "+registerManager.getClass().getCanonicalName());
                this.pullTriggerKeys.put(registerManagerId, TriggerKey.triggerKey("pullTrigger", registerManagerId));
                // Set up new schedule, or replace existing
                Trigger pullTrigger = TriggerBuilder.newTrigger()
                        .withIdentity(this.pullTriggerKeys.get(registerManagerId))
                        .withSchedule(
                                CronScheduleBuilder.cronSchedule(cronSchedule)
                        ).build();

                JobDataMap jobData = new JobDataMap();
                jobData.put(PullTask.DATA_ENGINE, this);
                jobData.put(PullTask.DATA_REGISTERMANAGER, registerManager);
                jobData.put(PullTask.DATA_DUMMYRUN, dummyRun);
                jobData.put(PullTask.DATA_SCHEDULE, cronSchedule);
                JobDetail job = JobBuilder.newJob(PullTask.class)
                        .withIdentity("pullTask-"+registerManagerId)
                        .setJobData(jobData)
                        .build();

                scheduler.scheduleJob(job, Collections.singleton(pullTrigger), true);
                scheduler.start();
            } else {
                // Remove old schedule
                this.log.info("Removing cron schedule to pull from " + registerManager.getClass().getCanonicalName());
                if (this.pullTriggerKeys.containsKey(registerManagerId)) {
                    scheduler.unscheduleJob(this.pullTriggerKeys.get(registerManagerId));
                }
            }

        } catch (SchedulerException e) {

        }
    }


    /** Synchronization **/

    /**
     * Synchronizes with the register, pulling a list of registration checksums and comparing it with what we already have.
     * For each registration we haven't seen before, pull the whole registration and store it in DB
     * @param session A database session to work on
     * @param plugin The plugin whose registermanager we interface with
     * @param from Optional date; only pull registration checksums after this date
     * @return New registrations added by this process
     * @throws DataFordelerException
     */
    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>, P extends RegistrationReference> List<R> synchronize(Session session, Plugin plugin, OffsetDateTime from) throws DataFordelerException {
        this.log.info("Synchronizing with plugin " + plugin.getClass().getCanonicalName());
        RegisterManager registerManager = plugin.getRegisterManager();
        ItemInputStream<? extends EntityReference> entityReferences = registerManager.listRegisterChecksums(null, from);
        EntityReference<E, P> entityReference;
        ArrayList<R> newRegistrations = new ArrayList<R>();
        try {
            while ((entityReference = entityReferences.next()) != null) {
                Class<E> entityClass = entityReference.getEntityClass();
                EntityManager entityManager = registerManager.getEntityManager(entityClass);
                E entity = this.queryManager.getEntity(session, entityReference.getObjectId(), entityClass);
                HashSet<String> knownChecksums = new HashSet<>();
                if (entity != null) {
                    for (Object oRegistration : entity.getRegistrations()) {
                        R registration = (R) oRegistration;
                        String checksum = registration.getRegisterChecksum();
                        this.log.debug("Checksum " + checksum + " is already known");
                        knownChecksums.add(checksum);
                    }
                }

                // We need to find all registrations that we don't already have in the database
                ArrayList<P> missingRegistrationReferences = new ArrayList<P>();
                for (P registrationReference : entityReference.getRegistrationReferences()) {
                    String checksum = registrationReference.getChecksum();
                    if (!knownChecksums.contains(checksum)) {
                        this.log.debug("Checksum " + checksum + " is not already known, adding to list of registrations to fetch");
                        missingRegistrationReferences.add(registrationReference);
                    }
                }

                for (P registrationReference : missingRegistrationReferences) {
                    List<? extends Registration> registrations = entityManager.fetchRegistration(registrationReference);
                    for (Registration registration : registrations) {
                        queryManager.saveRegistration(session, entity, (R) registration);
                        newRegistrations.add((R) registration);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataFordelerException e) {
            this.log.error("Synchronization with plugin "+plugin.getClass().getCanonicalName()+" failed", e);
        }
        return newRegistrations;
    }
}
