package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.PullJobListener;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import dk.magenta.datafordeler.plugindemo.DemoEntityManager;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by lars on 03-04-17.
 */
@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PullTest extends GapiTestBase {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private Engine engine;

    @LocalServerPort
    private int port;

    @Test
    @Order(order=1)
    public void pull() throws DataFordelerException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Plugin plugin = pluginManager.getPluginForSchema("Postnummer");
        DemoRegisterManager.setPortOnAll(this.port);

        String checksum = this.hash(UUID.randomUUID().toString());
        System.out.println("checksum: "+checksum);
        String reference = "http://localhost:"+this.port+"/test/get/" + checksum;
        String uuid = UUID.randomUUID().toString();
        String full = this.getPayload("/referencelookuptest.json")
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        ExpectorCallback lookupCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/get/" + checksum, full, lookupCallback);

        String event1 = this.envelopReference("Postnummer", reference);
        String body = this.jsonList(Collections.singletonList(event1), "events");
        System.out.println("body: "+body);
        ExpectorCallback eventCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/getNewEvents", body, eventCallback);

        ExpectorCallback receiptCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/receipt", "", receiptCallback);

        Pull pull = new Pull(engine, plugin);
        pull.run();
        //engine.fetchEvents(plugin);

        Assert.assertTrue(lookupCallback.get(20, TimeUnit.SECONDS));
        Assert.assertTrue(receiptCallback.get(20, TimeUnit.SECONDS));

        this.deleteEntity(uuid);
        DemoRegisterManager.setPortOnAll(Application.servicePort);
    }

    @Test
    @Order(order=2)
    public void schedule() throws SchedulerException, InterruptedException {
        Plugin plugin = pluginManager.getPluginForSchema("Postnummer");
        RegisterManager registerManager = plugin.getRegisterManager();
        String registerManagerId = registerManager.getClass().getName() + registerManager.hashCode();

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        ListenerManager listenerManager = scheduler.getListenerManager();
        PullJobListener pullJobListener = new PullJobListener("PullTest.schedule");
        listenerManager.addJobListener(pullJobListener, KeyMatcher.keyEquals(new JobKey("pullTask-" + registerManagerId)));

        // A schedule to fire every second
        // Because we're down to 'every second', it will also fire immediately
        this.waitToMilliseconds(500, 50);
        engine.setupPullSchedule(plugin.getRegisterManager(), "* * * * * ?", true);

        Thread.sleep(1000);
        // One second has passed, should now have executed exactly twice (initial + 1 second)
        Assert.assertEquals(2, pullJobListener.size(PullJobListener.Event.jobToBeExecuted));
        Assert.assertEquals(2, pullJobListener.size(PullJobListener.Event.jobWasExecuted));

        Thread.sleep(2000);
        // Three seconds have passed, should now have executed exactly four times (initial plus 3 seconds)
        Assert.assertEquals(4, pullJobListener.size(PullJobListener.Event.jobToBeExecuted));
        Assert.assertEquals(4, pullJobListener.size(PullJobListener.Event.jobWasExecuted));


        // A schedule to fire every two seconds
        this.waitToMilliseconds(500, 50);
        pullJobListener.reset(PullJobListener.Event.jobToBeExecuted);
        pullJobListener.reset(PullJobListener.Event.jobWasExecuted);
        pullJobListener.reset(PullJobListener.Event.jobExecutionVetoed);
        engine.setupPullSchedule(plugin.getRegisterManager(), "0/2 * * * * ?", true);

        boolean evenSecond = OffsetDateTime.now().getSecond() % 2 == 0;
        Thread.sleep(4000);
        Assert.assertEquals(evenSecond ? 3 : 2, pullJobListener.size(PullJobListener.Event.jobToBeExecuted));
        Assert.assertEquals(evenSecond ? 3 : 2, pullJobListener.size(PullJobListener.Event.jobWasExecuted));

        pullJobListener.reset(PullJobListener.Event.jobToBeExecuted);
        pullJobListener.reset(PullJobListener.Event.jobWasExecuted);
        pullJobListener.reset(PullJobListener.Event.jobExecutionVetoed);

        engine.setupPullSchedule(plugin.getRegisterManager(), null, true);
        Thread.sleep(2000);
        // Should not run any further
        Assert.assertEquals(0, pullJobListener.size(PullJobListener.Event.jobToBeExecuted));
        Assert.assertEquals(0, pullJobListener.size(PullJobListener.Event.jobWasExecuted));
    }

    @Test
    @Order(order=3)
    public void full() throws DataFordelerException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Plugin plugin = pluginManager.getPluginForSchema("Postnummer");
        DemoRegisterManager.setPortOnAll(this.port);
        String checksum = this.hash(UUID.randomUUID().toString());
        String reference = "http://localhost:"+this.port+"/test/get/" + checksum;
        String uuid = UUID.randomUUID().toString();
        String full = this.getPayload("/referencelookuptest.json")
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        ExpectorCallback lookupCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/get/" + checksum, full, lookupCallback);

        String event1 = this.envelopReference("Postnummer", reference);
        String body = this.jsonList(Collections.singletonList(event1), "events");
        ExpectorCallback eventCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/getNewEvents", body, eventCallback);

        ExpectorCallback receiptCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/receipt", "", receiptCallback);

        OffsetDateTime next = OffsetDateTime.now().plusSeconds(2);
        String cronSchedule = next.getSecond() + " " + next.getMinute() + " " + next.getHour() + " " + next.getDayOfMonth() + " " + next.getMonthValue() + " " + "?";

        DemoRegisterManager registerManager = (DemoRegisterManager) plugin.getRegisterManager();
        engine.setupPullSchedule(registerManager, cronSchedule, false);

        Assert.assertTrue(lookupCallback.get(20, TimeUnit.SECONDS));
        Assert.assertTrue(receiptCallback.get(20, TimeUnit.SECONDS));

        this.deleteEntity(uuid);
        DemoRegisterManager.setPortOnAll(Application.servicePort);
    }

    private String jsonList(List<String> jsonData, String listKey) {
        StringJoiner sj = new StringJoiner(",");
        for (String j : jsonData) {
            sj.add(j);
        }
        return "{\""+listKey+"\":["+sj.toString()+"]}";
    }

    // Because code doesn't execute instantaneously, there are often times when we wait for one second,
    // perform a time-sensitive check on whether a job has run, and it turns out that a little over one
    // second has passed, making the job run one more time than we expected, failing the test
    // E.g. start a x.998, wait 1000ms, check job. But now it's (x+2).001 (instead of (x+1).998), and the job has run three times, where we expected two
    // Solution: always start at around x.500
    private void waitToMilliseconds(int millis, int tolerance) throws InterruptedException {
        System.out.println(OffsetDateTime.now());
        int current = OffsetDateTime.now().getNano() / 1000000;
        int wait = 0;
        if (current > millis + tolerance) {
            wait = 1000 + millis - current;
        } else if (current < millis - tolerance) {
            wait = millis - current;
        }
        System.out.println("wait: "+wait);
        Thread.sleep(wait);
    }

}
