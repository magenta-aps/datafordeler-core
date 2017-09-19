package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.command.DumpCommandHandler;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.DumpData;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.SimilarJobRunningException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Created by lars on 29-05-17. A Runnable that performs a dump with a given
 * Query
 */
public class Dump extends Worker {

    private Logger log = LogManager.getLogger("Dump");

    private Engine engine;
    private String query, plugin;

    public Dump(Engine engine, DumpCommandHandler.DumpCommandData data) {
        this.engine = engine;
        this.query = data.query;
        this.plugin = data.plugin;
    }

    /**
     * Fetches and processes outstanding events from the Register Basically
     * DUMP
     */
    public void run() {
        try {
            if (runningDumps.keySet().contains(this.query)) {
                throw new SimilarJobRunningException(
                    "Another dump job is already running for Query "
                        + this.query
                        .getClass()
                        .getCanonicalName() + " (" + this.query.hashCode()
                        + ")");
            }

            this.log.info(
                "Worker " + this.getId() + " adding lock for " + this.query
                    .getClass()
                    .getCanonicalName()
                    + " (" + this.query.hashCode() + ")");
            runningDumps.put(this.query, this);

            this.log.info(
                "Worker " + this.getId() + " fetching events with " + this.query
                    .getClass()
                    .getCanonicalName());

            boolean error = false;

            // MEAT GOES HERE
            Class<? extends Entity> cls =
                this.engine.pluginManager.getPluginByName(this.plugin)
                    .getRegisterManager().getEntityManager(this.query)
                    .getManagedEntityClass();

            Session session =
                this.engine.sessionManager.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            OffsetDateTime now = OffsetDateTime.now();

            try {
                List<? extends Entity> entities =
                    this.engine.queryManager.getAllEntities(session, cls);

                this.log.info("dumping {} entities of type {}",
                    entities.size(), this.query);

                for (Entity entity : entities) {
                    // FIXME: this is horribly, horribly wrong
                    Registration reg = entity.getRegistrationAt(now);

                    String data = reg.toString();

                    session.save(new DumpData(this.plugin, this.query, data));
                }

                transaction.commit();
            } finally {
                session.close();
            }

            if (this.doCancel) {
                this.log.info("Worker " + this.getId() + ": Dump interrupted");
            } else if (error) {
                this.log.info("Worker " + this.getId() + ": Dump errored");
            } else {
                this.log.info("Worker " + this.getId() + ": Dump complete");
            }

            this.onComplete();

            this.log.info(
                "Worker " + this.getId() + " removing lock for " + this.query
                    .getClass()
                    .getCanonicalName() + " (" + this.query.hashCode() + ") on "
                    + OffsetDateTime.now()
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            runningDumps.remove(this.query);

        } catch (DataFordelerException e) {
            runningDumps.remove(this.query);
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final HashMap<String, Dump> runningDumps =
        new HashMap<>();

}
