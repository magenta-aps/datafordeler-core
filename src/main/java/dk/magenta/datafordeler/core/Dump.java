package dk.magenta.datafordeler.core;

import java.time.OffsetDateTime;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDataMap;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.DumpInfo;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.util.MockInternalServletRequest;

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

    static enum Format {
        xml(MediaType.APPLICATION_XML),
        json(MediaType.APPLICATION_JSON),
        csv(new MediaType("text", "csv")),
        tsv(new MediaType("text", "tsv")),;

        private final MediaType mediaType;

        Format(MediaType mediaType) {
            this.mediaType = mediaType;
        }
    }

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

            for (Plugin plugin : this.engine.pluginManager.getPlugins()) {
                for (EntityManager entityManager :
                    plugin.getRegisterManager().getEntityManagers()) {
                    for (String servicePath : entityManager.getEntityService()
                            .getServicePaths()) {
                        String requestPath = servicePath + "/search";

                        for (Format format : Format.values()) {
                            Transaction transaction = session.beginTransaction();

                            log.info("Dumping {} for {}", format, requestPath);
                            dump(requestPath, format.mediaType);

                            DumpInfo dump = new DumpInfo(plugin.getName(),
                                requestPath, format.name(), timestamp,
                                dump(requestPath, format.mediaType));

                            // first, delete pre-existing, comparable dumps
                            CriteriaBuilder criteriaBuilder = session
                                .getCriteriaBuilder();
                            CriteriaDelete<DumpInfo> criteria = criteriaBuilder
                                .createCriteriaDelete(DumpInfo.class);

                            Root<DumpInfo> root = criteria.from(DumpInfo.class);

                            criteria
                                .where(criteriaBuilder.equal(
                                    root.get("plugin"), plugin.getName()))
                                .where(criteriaBuilder.equal(
                                    root.get("entityName"), requestPath))
                                .where(criteriaBuilder.equal(
                                    root.get("format"), format.name()))
                                .where(criteriaBuilder.lessThan(
                                    root.get("timestamp"), timestamp));

                            session.createQuery(criteria).executeUpdate();

                            // then, save the dump
                            session.save(dump);
                            transaction.commit();
                        }
                    }
                }
            }


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
        String requestPath,
        MediaType mediaType) throws Exception {
        MockInternalServletRequest request =
            new MockInternalServletRequest("DUMP", requestPath);
        MockHttpServletResponse response =
            new MockHttpServletResponse();

        request.addHeader("Accept", mediaType);

        try {
            engine.handleRequest(request, response);
        } catch (HttpNotFoundException e) {
            return null;
        }

        return response.getContentAsString();
    }
}
