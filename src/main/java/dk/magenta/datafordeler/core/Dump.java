package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.DumpData;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Created by lars on 29-05-17. A Runnable that performs a dump with a given
 * Query
 */
public class Dump extends Worker {

    private static final String[] FORMATS = {"xml", "json"};

    private Logger log = LogManager.getLogger(this.getClass().getName());

    private Engine engine;

    public Dump(Engine engine) {
        this.engine = engine;
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
            OffsetDateTime now = OffsetDateTime.now();

            for (Plugin plugin : this.engine.pluginManager.getPlugins()) {
                for (EntityManager entityManager :
                    plugin.getRegisterManager().getEntityManagers()) {
                    String schema = entityManager.getSchema();

                    List<? extends Entity> entities =
                        this.engine.queryManager.getAllEntities(session,
                            entityManager.getManagedEntityClass());

                    this.log.info("dumping {} {} in {}",
                        entities.size(), schema,
                        plugin.getName());

                    // TODO: this is hugely inefficient, using loads of memory
                    List<Registration> regs = entities.stream().map(
                        e -> e.getRegistrationAt(now)
                    ).collect(Collectors.toList());

                    for (String format : FORMATS) {
                        session.save(
                            new DumpData(plugin.getName(),
                                schema, format, dump(regs, format), now)
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private String dump(List<? extends Registration> registrations, String
        format) {
        try {
            switch (format) {
                case "json":
                    return new ObjectMapper().writeValueAsString(registrations);

                case "xml": {
                    StringWriter w = new StringWriter();

                    XMLInputFactory inputFactory = XMLInputFactory.newFactory();
                    XMLOutputFactory outputFactory = XMLOutputFactory
                        .newFactory();

                    XMLStreamWriter writer = outputFactory
                        .createXMLStreamWriter(w);
                    XmlMapper mapper = new XmlMapper(inputFactory);

                    writer.writeStartDocument();
                    writer.writeStartElement("Registrations");
                    for (Registration r : registrations) {
                        mapper.writeValue(writer, r);
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
