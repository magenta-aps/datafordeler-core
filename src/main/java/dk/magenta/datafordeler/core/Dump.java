package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.DumpInfo;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDataMap;

/**
 * Created by lars on 29-05-17. A Runnable that performs a dump with a given
 * Query
 */
public class Dump extends Worker {
    public static class Task extends AbstractTask<Dump> {

        public static final String DATA_ENGINE = "engine";

        @Override
        protected Dump createWorker(JobDataMap dataMap) {
            return new Dump((Engine) dataMap.get(DATA_ENGINE));
        }
    }

    static final String[] FORMATS = {"xml", "json"};

    private Logger log = LogManager.getLogger(this.getClass().getName());

    private final Engine engine;
    private final OffsetDateTime timestamp;

    public Dump(Engine engine) {
        this.engine = engine;
        this.timestamp = OffsetDateTime.now();
    }

    public Dump(Engine engine, OffsetDateTime timestamp) {
        this.engine = engine;
        this.timestamp = timestamp;
    }

    /**
     * Fetches and processes outstanding events from the Register Basically
     * DUMP
     */
    public void run() {
        Session session =
            this.engine.sessionManager.getSessionFactory()
                .openSession();

        try {
            this.log.info("Worker {} is dumping the database", this.getId());

            Transaction transaction = session.beginTransaction();

            for (Plugin plugin : this.engine.pluginManager.getPlugins()) {
                for (EntityManager entityManager :
                    plugin.getRegisterManager().getEntityManagers()) {

                    String schema = entityManager.getSchema();

                    Class<? extends Entity> entityClass = entityManager
                        .getManagedEntityClass();
                    List<? extends Entity<? extends Entity, ? extends
                        Registration>> entities =
                        this.engine.queryManager.getAllEntities(session,
                            entityClass);

                    this.log.debug(
                        "Dumping {} entities of type '{}' from plugin '{}'",
                        entities.size(), schema,
                        plugin.getName());

                    // TODO: this is hugely inefficient, using loads of memory
                    List<Registration> regs = entities.stream().map(
                        e -> e.getRegistrationAt(timestamp)
                    ).collect(Collectors.toList());

                    for (String format : FORMATS) {
                        session.save(
                            new DumpInfo(plugin.getName(),
                                schema, format, timestamp,
                                dump(entityManager, regs, format))
                        );
                    }

                }
            }

            transaction.commit();

            if (this.doCancel) {
                this.log.info("Worker {}: Dump interrupted", this.getId());
            } else {
                this.log.info("Worker {}: Dump complete", this.getId());
            }

            this.onComplete();
        } catch (Throwable e) {
            this.log.error("Worker {}: Dump failed", e);
        } finally {
            session.close();
        }
    }

    private String dump(
        EntityManager entityManager,
        List<? extends Registration> registrations, String
        format) {
        try {
            switch (format) {
                case "json":
                    return entityManager.getObjectMapper().writeValueAsString
                        (registrations);

                case "xml": {
                    StringWriter w = new StringWriter();
                    XMLOutputFactory outputFactory = XMLOutputFactory
                        .newFactory();

                    XMLStreamWriter writer = outputFactory
                        .createXMLStreamWriter(w);

                    writer.writeStartDocument();
                    writer.writeStartElement("Registrations");
                    for (Registration r : registrations) {
                        entityManager.getXmlMapper().writeValue(writer, r);
                    }
                    writer.writeEndElement();
                    writer.writeEndDocument();

                    return w.toString();
                }

                default:
                    return null;
            }
        } catch (Exception e) {
            this.log.warn("dump failed", e);

            return null;
        }
    }
}
