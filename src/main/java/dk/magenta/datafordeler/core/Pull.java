package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.SimilarJobRunningException;
import dk.magenta.datafordeler.core.io.Event;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Created by lars on 29-05-17.
 */
public class Pull extends Worker implements Runnable {

    private Logger log = LogManager.getLogger("Pull");

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

            this.log.info(this.getId()+" Adding lock for " + this.registerManager.getClass().getCanonicalName() + " ("+this.registerManager.hashCode()+")");
            runningPulls.put(this.registerManager, this);

            this.log.info(this.getId()+" Fetching events with " + this.registerManager.getClass().getCanonicalName());
            ItemInputStream<Event> eventStream = this.registerManager.pullEvents();
            int count = 0;
            try {
                Event event;
                while ((event = eventStream.next()) != null && !this.doCancel) {
                    if (!this.engine.handleEvent(event, this.registerManager.getPlugin())) {
                        this.log.warn("Failed handling event " + event.getEventID() + ", not processing further events");
                        eventStream.close();
                        break;
                    }
                    count++;
                }
            } catch (IOException e) {
                DataStreamException ex = new DataStreamException(e);
                this.onError(ex);
                throw ex;
            } finally {
                this.log.info("Processed " + count + " events. Closing stream.");
                eventStream.close();
            }

            if (this.doCancel) {
                this.log.info("Pull interrupted");
            } else {
                this.log.info("Pull complete");
            }
            this.onComplete();

            this.log.info("Removing lock for " + this.registerManager.getClass().getCanonicalName() + " ("+this.registerManager.hashCode()+") on " + OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
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
