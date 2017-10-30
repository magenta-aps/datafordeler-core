package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
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
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 29-05-17.
 * A Runnable that performs a pull with a given RegisterManager
 */
public class Pull extends Worker implements Runnable {

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
            if (this.registerManager.pullsEventsCommonly()) {
                this.log.info("Pulling data for "+this.registerManager.getClass().getSimpleName());
                ItemInputStream<? extends PluginSourceData> stream = this.registerManager.pullEvents();
                this.doPull(importMetadata, stream);
                // Done. Write last-updated timestamp
                this.registerManager.setLastUpdated(null, importMetadata.getImportTime());
            } else {
                for (EntityManager entityManager : this.registerManager.getEntityManagers()) {
                    this.log.info("Pulling data for "+entityManager.getClass().getSimpleName());
                    ItemInputStream<? extends PluginSourceData> stream = this.registerManager.pullEvents(this.registerManager.getEventInterface(entityManager), entityManager);
                    this.doPull(importMetadata, stream);
                    // Done. Write last-updated timestamp
                    this.registerManager.setLastUpdated(entityManager, importMetadata.getImportTime());
                }
            }

            if (this.doCancel) {
                this.log.info("Worker " + this.getId() + ": Pull interrupted");
            } else if (error) {
                this.log.info("Worker " + this.getId() + ": Pull errored");
            } else {
                this.log.info("Worker " + this.getId() + ": Pull complete");
            }
            this.onComplete();

            this.log.info("Worker " + this.getId() + " removing lock for " + this.registerManager.getClass().getCanonicalName() + " ("+this.registerManager.hashCode()+") on " + OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        } catch (Throwable e) {
            this.log.error(e);
            this.onError(e);
            throw new RuntimeException(e);
        } finally {
            runningPulls.remove(this.registerManager);
        }
    }

    private void doPull(ImportMetadata importMetadata, ItemInputStream<? extends PluginSourceData> eventStream) throws DataStreamException, IOException {

        int count = 0;
        try {
            PluginSourceData event;
            while ((event = eventStream.next()) != null && !this.doCancel) {
                if (!this.engine.handleEvent(event, this.registerManager.getPlugin(), importMetadata)) {
                    this.log.warn("Worker " + this.getId() + " failed handling event " + event.getId() + ", not processing further events");
                    eventStream.close();
                    break;
                }
                count++;
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
