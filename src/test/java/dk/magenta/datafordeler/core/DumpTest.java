package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.DumpData;
import dk.magenta.datafordeler.core.database.DumpInfo;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.TaskListener;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import dk.magenta.datafordeler.plugindemo.model.DemoData;
import dk.magenta.datafordeler.plugindemo.model.DemoEffect;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

/**
 * Created by lars on 03-04-17.
 */
@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DumpTest extends GapiTestBase {

    private Logger log = LogManager.getLogger(this.getClass().getName());

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private Engine engine;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private QueryManager queryManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    /**
     * Perform some sanity checks before each test.
     */
    @Before
    public void setUp() throws Exception {
        List<String> entityNames = engine.pluginManager.getPlugins().stream()
            .flatMap(
                p -> p.getRegisterManager().getEntityManagers().stream()
            ).map(
                em -> String.format("%s (%s)",
                    em.getManagedEntityClass().getCanonicalName(),
                    em.getSchema())
            ).collect(Collectors.toList());

        log.info("{} entities: {}",
            entityNames.size(),
            entityNames.stream().collect(Collectors.joining(", ")));

        Assert.assertNotEquals("At least one entity required", 0,
            entityNames);

        Session session = sessionManager.getSessionFactory().openSession();
        Assert.assertTrue(
            "no pre-existing dumps allowed",
            queryManager.getAllItems(session, DumpInfo.class).isEmpty()
        );
        Assert.assertTrue(
            "no pre-existing dumps allowed",
            queryManager.getAllItems(session, DumpData.class).isEmpty()
        );
        session.close();
    }

    @After
    public void tearDown() throws Exception {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        for (Class<? extends DatabaseEntry> cls : Arrays.asList(
            DemoRegistration.class,
            DemoEffect.class,
            DemoData.class,
            DemoEntity.class,
            Identification.class,
            DumpInfo.class,
            DumpData.class
        )) {
            for (DatabaseEntry entry : queryManager.getAllItems(session, cls)) {
                session.delete(entry);
            }
        }
        transaction.commit();
        session.close();
        setUp();
    }

    private void createOneEntity(int postalcode, String cityname)
        throws DataFordelerException {
        final OffsetDateTime from =
            OffsetDateTime.parse("2001-01-01T00:00:00+00:00");

        DemoEntity entity = new DemoEntity(
            new UUID(0, Integer.parseInt(Integer.toString(postalcode), 16)),
            "http://example.com"
        );
        DemoRegistration registration = new DemoRegistration(from, null, 0);
        entity.addRegistration(registration);

        DemoEffect effect = new DemoEffect(registration, from, null);
        effect.setDataItems(Arrays.asList(
            new DemoData(postalcode, cityname)
        ));
        registration.addEffect(effect);

        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        queryManager.saveRegistration(session, entity, registration);
        transaction.commit();
        session.close();
    }

    private void createOneEntity() throws DataFordelerException {
        createOneEntity(3900, "Nuuk");
    }

    /**
     * Test scheduling.
     */
    @Test
    public void schedule() throws SchedulerException, InterruptedException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        ListenerManager listenerManager = scheduler.getListenerManager();
        TaskListener taskListener = new TaskListener("DumpTest.schedule");
        listenerManager.addJobListener(
            taskListener,
            KeyMatcher.keyEquals(new JobKey("DUMP"))
        );
        SimpleScheduleBuilder scheduleBuilder =
            SimpleScheduleBuilder.simpleSchedule().repeatForever();

        // A schedule to fire every second
        // Because we're down to 'every second', it will also fire immediately
        engine.setupDumpSchedule(
            scheduleBuilder.withIntervalInMilliseconds(100),
            true);

        Thread.sleep(100);
        // One second has passed, should now have executed exactly twice (initial + 1 second)
        Assert.assertEquals(2,
            taskListener.size(TaskListener.Event.jobToBeExecuted));
        Assert.assertEquals(2,
            taskListener.size(TaskListener.Event.jobWasExecuted));

        Thread.sleep(250);
        // Three seconds have passed, should now have executed exactly four times (initial plus 3 seconds)
        Assert.assertEquals(4, taskListener.size(TaskListener.Event
            .jobToBeExecuted));
        Assert.assertEquals(4, taskListener.size(TaskListener.Event
            .jobWasExecuted));

        // A schedule to fire every two seconds
        taskListener.reset();
        engine.setupDumpSchedule(scheduleBuilder.withIntervalInMilliseconds
                (200),
            true);

        Thread.sleep(450);
        Assert.assertEquals(3, taskListener.size(TaskListener.Event
            .jobToBeExecuted));
        Assert.assertEquals(3, taskListener.size(TaskListener.Event
            .jobWasExecuted));

        taskListener.reset();

        engine.setupDumpSchedule((ScheduleBuilder) null, true);
        Thread.sleep(500);
        // Should not run any further
        Assert.assertEquals(0,
            taskListener.size(TaskListener.Event.jobToBeExecuted));
        Assert.assertEquals(0,
            taskListener.size(TaskListener.Event.jobWasExecuted));
    }

    /**
     * Test empty dumps.
     */
    @Test
    public void emptyDump() throws Exception {
        Plugin plugin = pluginManager.getPluginForSchema("Postnummer");

        {
            Session session = sessionManager.getSessionFactory().openSession();

            Assert.assertEquals("Initial state; no dumps", 0,
                queryManager.getAllItems(session, DumpInfo.class).size());
            session.close();
        }

        new Dump(this.engine).run();

        Session session = sessionManager.getSessionFactory().openSession();

        List<DumpInfo> dumps =
            queryManager.getAllItems(session, DumpInfo.class);

        Assert.assertEquals("After one run",
            1 * Dump.FORMATS.length, dumps.size());

        Assert.assertArrayEquals("Dump contents",
            new String[]{
                "<?xml version='1.0' encoding='UTF-8'?><Registrations/>",
                "{ }",
                null,
                null,
            },
            dumps.stream().map(d -> d.getData()).toArray());

        session.close();
    }

    /**
     * Test dumping actual entries.
     */
    @Test
    public void actualDump() throws Exception {
        Session session;
        createOneEntity(3900, "Nuuk");
        createOneEntity(3992, "Siriuspatruljen");

        session = sessionManager.getSessionFactory().openSession();

        Assert.assertEquals("Initial state; no dumps", 0,
            queryManager.getAllItems(session, DumpInfo.class).size());

        session.close();

        new Dump(this.engine).run();

        session = sessionManager.getSessionFactory().openSession();

        List<DumpInfo> dumps =
            queryManager.getAllItems(session, DumpInfo.class);

        Assert.assertEquals("After one run",
            1 * Dump.FORMATS.length, dumps.size());

        Assert.assertArrayEquals("Dump contents",
            Arrays.stream(Dump.FORMATS).map(
                s -> {
                    try {
                        return getPayload("/dump." + s);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            ).toArray(),
            dumps.stream().map(d -> d.getData()).toArray());

        session.close();
    }

    /**
     * The actual dump data might be rather large, so check that we don't load
     * it willy-nilly.
     */
    @Test
    @Transactional
    public void lazyLoad() throws Exception {
        createOneEntity();

        Session session = sessionManager.getSessionFactory().openSession();

        new Dump(this.engine).run();

        List<DumpInfo> dumps =
            queryManager.getAllItems(session, DumpInfo.class);

        session.close();

        Assert.assertEquals("After one run",
            1 * Dump.FORMATS.length, dumps.size());

        for (DumpInfo dump : dumps) {
            try {
                dump.getData();
                Assert.fail("should have failed data access");
            } catch (LazyInitializationException exc) {
                log.info("yes, this is exactly right", exc);
            }
        }
    }

    /**
     * Simple sanity test of the index.
     */
    @Test
    @Order(order = 4)
    @Transactional
    public void index() throws Exception {
        createOneEntity();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> resp = this.restTemplate
            .exchange("/", HttpMethod.GET, httpEntity, String.class);
        JsonNode json = objectMapper.readTree(resp.getBody());

        Assert.assertNotNull(json);
    }
}
