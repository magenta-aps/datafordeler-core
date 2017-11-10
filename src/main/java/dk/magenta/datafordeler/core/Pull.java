package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.SimilarJobRunningException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDataMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by lars on 29-05-17.
 * A Runnable that performs a pull with a given RegisterManager
 */
public class Pull extends Worker implements Runnable {
    /**
     * Created by lars on 06-04-17.
     */
    public static class Task extends AbstractTask<Pull> {
        public static final String DATA_ENGINE = "engine";
        public static final String DATA_REGISTERMANAGER = "registerManager";

        @Override
        protected Pull createWorker(JobDataMap dataMap) {
            Engine engine = (Engine) dataMap.get(DATA_ENGINE);
            RegisterManager registerManager = (RegisterManager) dataMap.get(DATA_REGISTERMANAGER);
            return new Pull(engine, registerManager);
        }
    }

    private Logger log = LogManager.getLogger(Pull.class);

    private RegisterManager registerManager;
    private Engine engine;

    public Pull(Engine engine, RegisterManager registerManager) {
        this.engine = engine;
        this.registerManager = registerManager;
    }

    public Pull(Engine engine, Plugin plugin) {
        this(engine, plugin.getRegisterManager());
    }

    /**
     * Fetches and processes outstanding events from the Register
     * Basically PULL
     * */
    public void run() {
        try {
            if (runningPulls.keySet().contains(this.registerManager)) {
                throw new SimilarJobRunningException("Another pull job is already running for RegisterManager "+this.registerManager.getClass().getCanonicalName()+" ("+this.registerManager.hashCode()+")");
            }

            this.log.info("Worker "+this.getId()+" adding lock for " + this.registerManager.getClass().getCanonicalName() + " ("+this.registerManager.hashCode()+")");
            runningPulls.put(this.registerManager, this);

            this.log.info("Worker "+this.getId()+" fetching events with " + this.registerManager.getClass().getCanonicalName());

            ImportMetadata importMetadata = new ImportMetadata();

            boolean error = false;
            boolean skip = false;
            if (this.registerManager.pullsEventsCommonly()) {
                this.log.info("Pulling data for "+this.registerManager.getClass().getSimpleName());
                ItemInputStream<? extends PluginSourceData> stream = this.registerManager.pullEvents();
                if (stream != null) {
                    this.doPull(importMetadata, stream);
                    // Done. Write last-updated timestamp
                    this.registerManager.setLastUpdated(null, importMetadata.getImportTime());
                } else {
                    skip = true;
                }
            } else {
                for (EntityManager entityManager : this.registerManager.getEntityManagers()) {
                    this.log.info("Pulling data for "+entityManager.getClass().getSimpleName());

                    /*
                    ItemInputStream<? extends PluginSourceData> stream = this.registerManager.pullEvents(this.registerManager.getEventInterface(entityManager), entityManager);
                    if (stream != null) {
                        this.doPull(importMetadata, stream);
                        // Done. Write last-updated timestamp
                        this.registerManager.setLastUpdated(entityManager, importMetadata.getImportTime());
                    } else {
                        skip = true;
                    }*/

                    InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager);

                    Session session = this.engine.sessionManager.getSessionFactory().openSession();
                    Transaction transaction = session.beginTransaction();
                    importMetadata.setSession(session);

                    try {
                        entityManager.parseRegistration(stream, importMetadata);
                        session.flush();
                        session.clear();
                        transaction.commit();
                    } catch (Exception e) {
                        transaction.rollback();
                        e.printStackTrace();
                        throw e;
                    } finally {
                        session.close();
                    }
                }
            }

            String prefix = "Worker " + this.getId() + ": ";
            if (this.doCancel) {
                this.log.info(prefix + "Pull interrupted");
            } else if (error) {
                this.log.info(prefix + "Pull errored");
            } else if (skip) {
                this.log.info(prefix + "Pull skipped");
            } else {
                this.log.info(prefix + "Pull complete");
            }
            this.onComplete();

            this.log.info("Worker " + this.getId() + " removing lock for " + this.registerManager.getClass().getCanonicalName() + " ("+this.registerManager.hashCode()+") on " + OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        } catch (Throwable e) {
            e.printStackTrace();
            this.log.error(e);
            this.onError(e);
            throw new RuntimeException(e);
        } finally {
            runningPulls.remove(this.registerManager);
        }
    }

    private void doPull(ImportMetadata importMetadata, ItemInputStream<? extends PluginSourceData> eventStream) throws DataStreamException, IOException {
        this.log.info("doPull");
        int count = 0;
        try {
            PluginSourceData event;
            Plugin plugin = this.registerManager.getPlugin();
            while ((event = eventStream.next()) != null && !this.doCancel) {
                boolean success = this.engine.handleEvent(event, plugin, importMetadata);
                log.info("Success: "+success);
                if (!success) {
                    this.log.warn("Worker " + this.getId() + " failed handling event " + event.getId() + ", not processing further events");
                    eventStream.close();
                    break;
                }
                count++;
//                if (count >= 30) {
//                    log.info("Stopping after 30 events");
//                    break;
//                }
            }

        } catch (IOException e) {
            throw new DataStreamException(e);
        } finally {
            this.log.info("Worker " + this.getId() + " processed " + count + " events. Closing stream.");
            eventStream.close();
        }
    }

    private static HashMap<RegisterManager, Pull> runningPulls = new HashMap<>();

}
