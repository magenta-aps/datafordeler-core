package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.SimilarJobRunningException;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;

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

            boolean error = false;
            Collection<ItemInputStream<? extends PluginSourceData>> streams = this.registerManager.pullEvents();
            for (ItemInputStream<? extends PluginSourceData> eventStream : streams) {

                int count = 0;
                try {
                    PluginSourceData event;
                    while ((event = eventStream.next()) != null && !this.doCancel) {
                        if (!this.engine.handleEvent(event, this.registerManager.getPlugin())) {
                            this.log.warn("Worker " + this.getId() + " failed handling event " + event.getId() + ", not processing further events");
                            eventStream.close();
                            break;
                        }
                        count++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    DataStreamException ex = new DataStreamException(e);
                    this.onError(ex);
                    error = true;
                    throw ex;
                } catch (Throwable e) {
                    error = true;
                    e.printStackTrace();
                } finally {
                    this.log.info("Worker " + this.getId() + " processed " + count + " events. Closing stream.");
                    eventStream.close();
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
            runningPulls.remove(this.registerManager);

        } catch (DataFordelerException e) {
            runningPulls.remove(this.registerManager);
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HashMap<RegisterManager, Pull> runningPulls = new HashMap<>();

}
