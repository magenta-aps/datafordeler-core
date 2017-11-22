package dk.magenta.datafordeler.core.dump;

import dk.magenta.datafordeler.core.AbstractTask;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.DumpInfo;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.util.MockInternalServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDataMap;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 29-05-17. A Runnable that performs a dump with a given
 * Query
 */
public class Dump extends Worker {

    private final SessionManager sessionManager;

    public static class Task extends AbstractTask<Dump> {

        public static final String DATA_ENGINE = "engine";
        public static final String DATA_SESSIONMANAGER = "sessionManager";
        public static final String DATA_CONFIG = "config";

        @Override
        protected Dump createWorker(JobDataMap dataMap) {
            return new Dump(
                (Engine) dataMap.get(DATA_ENGINE),
                (SessionManager) dataMap.get(DATA_SESSIONMANAGER),
                (DumpConfiguration) dataMap.get(DATA_CONFIG)
            );
        }
    }

    private Logger log = LogManager.getLogger(this.getClass().getName());

    private final Engine engine;
    private final DumpConfiguration config;
    private final OffsetDateTime timestamp;

    public Dump(
        Engine engine,
        SessionManager sessionManager,
        DumpConfiguration config
    ) {
        this.engine = engine;
        this.sessionManager = sessionManager;
        this.config = config;
        this.timestamp = OffsetDateTime.now();
    }

    public Dump(
        Engine engine,
        SessionManager sessionManager,
        DumpConfiguration config,
        OffsetDateTime timestamp
    ) {
        this.engine = engine;
        this.sessionManager = sessionManager;
        this.config = config;
        this.timestamp = timestamp;
    }

    /**
     * Fetches and processes outstanding events from the Register Basically
     * DUMP
     */
    public void run() {
        Session session = sessionManager.getSessionFactory().openSession();

        try {
            this.log.info("Worker {} is executing dump {} of {} for {}",
                this.getId(), config.getId(), config.getFormat(),
                config.getRequestPath());

            Transaction transaction = session.beginTransaction();

            // TODO: is executing the dump within a transaction excessive?
            byte[] dumpData = dump(
                config.getRequestPath(),
                config.getFormat().getMediaType(),
                config.getCharset()
            );

            DumpInfo dump = new DumpInfo(config, timestamp, dumpData);

            Map<String, Object> filter = new HashMap<>();
            filter.put("name", config.getName());

            for (DumpInfo older : QueryManager.getItems(
                session, DumpInfo.class, filter)) {
                if (older.getTimestamp().isBefore(timestamp)) {
                    session.delete(older);
                }
            }

            // then, save the dump
            session.save(dump);

            transaction.commit();

            this.log.info("Worker {}: Dump complete", this.getId());

            this.onComplete();
        } catch (Throwable e) {
            this.log.error("Worker {}: Dump failed", e);
        } finally {
            session.close();
        }

    }

    private byte[] dump(String requestPath,
        MediaType mediaType, Charset charset)
        throws Exception {
        MockInternalServletRequest request =
            new MockInternalServletRequest("DUMP", requestPath);
        MockHttpServletResponse response =
            new MockHttpServletResponse();

        request.addHeader("Accept", mediaType);
        request.addHeader("Accept-Charset", charset);

        try {
            engine.handleRequest(request, response);
        } catch (HttpNotFoundException e) {
            return null;
        }

        return response.getContentAsByteArray();
    }
}
