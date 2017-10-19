package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
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

    static final String[] FORMATS = {"xml", "json", "csv", "tsv"};

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
                    CriteriaBuilder criteriaBuilder = session
                        .getCriteriaBuilder();

                    Class<? extends Entity> entityClass = entityManager
                        .getManagedEntityClass();
                    List<? extends Entity> entities =
                        QueryManager.getAllEntities(session,
                            entityClass);

                    this.log.debug(
                        "Dumping {} entities of type '{}' from plugin '{}'",
                        entities.size(), schema,
                        plugin.getName());

                    // TODO: this is hugely inefficient, using loads of memory
                    Map<String, ? extends Registration> regs = entities.stream()
                        .collect(Collectors.toMap(
                            e -> e.getUUID().toString(),
                            e -> e.getRegistrationAt(timestamp))
                        );

                    for (String format : FORMATS) {
                        DumpInfo dump = new DumpInfo(plugin.getName(),
                            schema, format, timestamp,
                            dump(entityManager, regs, format));

                        log.info("Saving {}", dump);

                        // first, delete pre-existing, comparable dumps
                        List<DumpInfo> olderDumps = session.createQuery(
                            "SELECT d FROM DumpInfo d WHERE " +
                                "d.plugin = :plugin AND " +
                                "d.entityName = :schema AND " +
                                "d.format = :format AND " +
                                "d.timestamp < :timestamp",
                            DumpInfo.class
                        )
                            .setParameter("schema", schema)
                            .setParameter("plugin", plugin.getName())
                            .setParameter("format", format)
                            .setParameter("timestamp", timestamp)
                            .getResultList();

                        for (DumpInfo olderDump : olderDumps) {
                            log.info("Deleting older {}", olderDump);
                            session.delete(olderDump);
                        }

                        // then, save the dump
                        session.save(dump);
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
        Map<String, ? extends Registration> registrations, String
        format) {
        try {
            switch (format) {
                case "json":
                    ObjectMapper objectMapper = entityManager.getObjectMapper();
                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                    return objectMapper.writeValueAsString
                        (registrations);

                case "xml": {
                    StringWriter w = new StringWriter();
                    XmlMapper xmlMapper = entityManager.getXmlMapper();
                    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

                    XMLOutputFactory outputFactory = XMLOutputFactory
                        .newFactory();
                    XMLStreamWriter writer = outputFactory
                        .createXMLStreamWriter(w);

                    writer.writeStartDocument();
                    writer.writeStartElement("Registrations");
                    for (Registration r : registrations.values()) {
                        xmlMapper.writeValue(writer, r);
                    }
                    writer.writeEndElement();
                    writer.writeEndDocument();

                    return w.toString();
                }

                case "tsv":
                case "csv": {
                    Iterator<Map<String, Object>> dataIter =
                        registrations.values().stream()
                            .flatMap(
                                reg -> ((List<Effect>) reg.getEffects())
                                    .stream()
                            ).map(
                            obj -> {
                                Effect e = (Effect) obj;
                                Registration r = e.getRegistration();
                                Map<String, Object> data = e.getData();

                                data.put("registrationFrom",
                                    r.getRegistrationFrom());
                                data.put("registrationTo",
                                    r.getRegistrationFrom());
                                data.put("sequenceNumber",
                                    r.getSequenceNumber());
                                data.put("uuid", r.getEntity().getUUID());

                                return data;
                            }
                        ).iterator();

                    if (!dataIter.hasNext()) {
                        return null;
                    }

                    CsvMapper mapper = entityManager.getCsvMapper();
                    CsvSchema.Builder builder =
                        new CsvSchema.Builder();

                    Map<String, Object> first = dataIter.next();
                    ArrayList<String> keys =
                        new ArrayList<>(first.keySet());
                    Collections.sort(keys);

                    for (int i = 0; i < keys.size(); i++) {
                        builder.addColumn(new CsvSchema.Column(
                            i, keys.get(i),
                            CsvSchema.ColumnType.NUMBER_OR_STRING
                        ));
                    }

                    CsvSchema schema = builder.build().withHeader();

                    if (format.equals("tsv")) {
                        schema = schema.withColumnSeparator('\t');
                    }

                    StringWriter w = new StringWriter();
                    SequenceWriter writer =
                        mapper.writer(schema).writeValues(w);

                    writer.write(first);

                    while (dataIter.hasNext()) {
                        writer.write(dataIter.next());
                    }

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
