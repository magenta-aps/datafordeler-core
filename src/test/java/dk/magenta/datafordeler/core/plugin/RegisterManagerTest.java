package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.EntityReference;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.testutil.CallbackController;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.core.testutil.KeyExpectorCallback;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityReference;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by lars on 15-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RegisterManagerTest extends PluginTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected CallbackController callbackController;

    @Autowired
    private DemoRegisterManager demoRegisterManager;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private class OtherEntity extends Entity {
        @Override
        protected Registration createEmptyRegistration() {
            return null;
        }
    }

    @Test
    public void testLinks() throws URISyntaxException {
        System.out.println("AppConfig.servicePort: "+ Application.servicePort);
        EntityManager entityManager = this.plugin.getEntityManager(DemoEntity.schema);
        RegisterManager registerManager = this.plugin.getRegisterManager();
        Assert.assertEquals(entityManager, registerManager.getEntityManager(DemoEntity.class));
        Assert.assertEquals(entityManager, registerManager.getEntityManager(DemoEntity.schema));
        Assert.assertEquals(entityManager, registerManager.getEntityManager(new URI("http://localhost:" + this.port)));
        Assert.assertEquals(entityManager, registerManager.getEntityManager(new URI("http://localhost:" + this.port + "/foo")));
        Assert.assertNull(registerManager.getEntityManager(new URI("http://localhost:" + (this.port + 1))));

        Assert.assertNotEquals(entityManager, registerManager.getEntityManager(OtherEntity.class));
    }

    @Test
    public void testHandlesSchema() {
        Assert.assertTrue(this.plugin.handlesSchema(DemoEntity.schema));
        Assert.assertFalse(this.plugin.handlesSchema("foobar"));
        Assert.assertFalse(this.plugin.handlesSchema(DemoEntity.schema+"a"));
    }

    @Test
    public void testGetHandledURISubstrings() {
        RegisterManager registerManager = this.plugin.getRegisterManager();
        Assert.assertTrue(registerManager.getHandledURISubstrings().contains("http://localhost:" + this.port));
        Assert.assertFalse(registerManager.getHandledURISubstrings().contains("http://localhost:" + (this.port + 1)));
        Assert.assertFalse(registerManager.getHandledURISubstrings().contains("http://localhost:" + this.port + "/foobar"));
    }

    @Test
    public void listChecksumsByTimestampTest() throws DataFordelerException, IOException {
        RegisterManager registerManager = this.plugin.getRegisterManager();

        KeyExpectorCallback listChecksumsCallback = new KeyExpectorCallback("from", "2017-01-01");
        String response = this.getPayload("/listchecksums_date_response.json");
        this.callbackController.addCallbackResponse("/test/listChecksums", response, listChecksumsCallback);

        for (OffsetDateTime time : new OffsetDateTime[]{null, OffsetDateTime.parse("2017-01-01T00:00:00Z")}) {
            ItemInputStream<? extends EntityReference> itemStream = registerManager.listRegisterChecksums(DemoEntity.schema, time);
            ArrayList<EntityReference> allReferences = new ArrayList<>();
            EntityReference entityReference;
            try {
                while ((entityReference = itemStream.next()) != null) {
                    allReferences.add(entityReference);
                }
            } catch (EOFException e) {
            }
            Assert.assertEquals(2, allReferences.size());

            DemoEntityReference firstReference = (DemoEntityReference) allReferences.get(0);
            Assert.assertEquals("Postnummer", firstReference.getType());
            Assert.assertEquals(UUID.fromString("2eed5323-f94f-5a9f-9607-39739c3585b5"), firstReference.getObjectId());
            Assert.assertEquals(1, firstReference.getRegistrationReferences().size());
            Assert.assertEquals(1, firstReference.getRegistrationReferences().get(0).getSequenceNumber());
            Assert.assertEquals("2a2ef17de084b10fe30ca726e2168c2e", firstReference.getRegistrationReferences().get(0).getChecksum());

            DemoEntityReference secondReference = (DemoEntityReference) allReferences.get(1);
            Assert.assertEquals("Postnummer", secondReference.getType());
            Assert.assertEquals(UUID.fromString("2eed5323-f94f-5a9f-9607-39739c3585b6"), secondReference.getObjectId());
            Assert.assertEquals(1, secondReference.getRegistrationReferences().size());
            Assert.assertEquals(2, secondReference.getRegistrationReferences().get(0).getSequenceNumber());
            Assert.assertEquals("2a2ef17de084b10fe30ca726e2168c2f", secondReference.getRegistrationReferences().get(0).getChecksum());
        }
        this.callbackController.removeCallback("/test/listChecksums");
    }


    @Test
    public void listChecksumsByTimestampTest2() throws DataFordelerException, IOException {
        RegisterManager registerManager = this.plugin.getRegisterManager();

        KeyExpectorCallback listChecksumsCallback = new KeyExpectorCallback("from", "2017-01-01");
        String response = this.getPayload("/listchecksums_date_response.json");
        this.callbackController.addCallbackResponse("/test/listChecksums", response, listChecksumsCallback);

        for (OffsetDateTime time : new OffsetDateTime[]{null, OffsetDateTime.parse("2017-01-01T00:00:00Z")}) {
            for (String schema : new String[]{null, "foobar"}) {
                ItemInputStream<? extends EntityReference> itemStream = registerManager.listRegisterChecksums(schema, time);
                ArrayList<EntityReference> allReferences = new ArrayList<>();
                EntityReference entityReference;
                try {
                    while ((entityReference = itemStream.next()) != null) {
                        System.out.println("entityReference:" +entityReference);
                        allReferences.add(entityReference);
                    }
                } catch (EOFException e) {
                }
                Assert.assertEquals(2, allReferences.size());

                DemoEntityReference firstReference = (DemoEntityReference) allReferences.get(0);
                Assert.assertEquals("Postnummer", firstReference.getType());
                Assert.assertEquals(UUID.fromString("2eed5323-f94f-5a9f-9607-39739c3585b5"), firstReference.getObjectId());
                Assert.assertEquals(1, firstReference.getRegistrationReferences().size());
                Assert.assertEquals(1, firstReference.getRegistrationReferences().get(0).getSequenceNumber());
                Assert.assertEquals("2a2ef17de084b10fe30ca726e2168c2e", firstReference.getRegistrationReferences().get(0).getChecksum());

                DemoEntityReference secondReference = (DemoEntityReference) allReferences.get(1);
                Assert.assertEquals("Postnummer", secondReference.getType());
                Assert.assertEquals(UUID.fromString("2eed5323-f94f-5a9f-9607-39739c3585b6"), secondReference.getObjectId());
                Assert.assertEquals(1, secondReference.getRegistrationReferences().size());
                Assert.assertEquals(2, secondReference.getRegistrationReferences().get(0).getSequenceNumber());
                Assert.assertEquals("2a2ef17de084b10fe30ca726e2168c2f", secondReference.getRegistrationReferences().get(0).getChecksum());
            }
        }
        this.callbackController.removeCallback("/test/listChecksums");
    }


    @Test
    public void testExpandBaseUri() throws URISyntaxException {
        Assert.assertEquals(new URI("http://localhost/foo/bar"), RegisterManager.expandBaseURI(new URI("http://localhost"), "/foo/bar"));
        Assert.assertEquals(new URI("http://localhost/foo/bar?bat=42#hey"), RegisterManager.expandBaseURI(new URI("http://localhost"), "/foo/bar", "bat=42", "hey"));
        Assert.assertEquals(new URI("http://localhost/base/foo/bar?bat=42#hey"), RegisterManager.expandBaseURI(new URI("http://localhost/base"), "/foo/bar", "bat=42", "hey"));
        Assert.assertEquals(new URI("http://localhost/foo/bar?bat=42#hey"), RegisterManager.expandBaseURI(new URI("http", null, "localhost", -1, null, null, null), "/foo/bar", "bat=42", "hey"));
    }


    @Test
    public void testPullEvents() throws DataFordelerException, IOException, InterruptedException, ExecutionException, TimeoutException {

        String checksum = this.hash(UUID.randomUUID().toString());
        this.demoRegisterManager.setPort(port);
        String reference = "http://localhost:" + port + "/test/get/" + checksum;
        String uuid = UUID.randomUUID().toString();
        String full = this.getPayload("/referencelookuptest.json")
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        String event1 = this.envelopReference("Postnummer", reference);
        String body = this.jsonList(Collections.singletonList(event1), "events");
        ExpectorCallback eventCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/getNewEvents", body, eventCallback);

        ItemInputStream<? extends PluginSourceData> dataStream = this.plugin.getRegisterManager().pullEvents();

        PluginSourceData data;
        int eventCounter = 0;
        while ((data = dataStream.next()) != null) {
            eventCounter++;
            Assert.assertEquals("msgid", data.getId());
            Assert.assertEquals(reference, data.getReference());
        }
        Assert.assertEquals(1, eventCounter);


        this.callbackController.removeCallback("/test/get/" + checksum);
        this.callbackController.removeCallback("/test/getNewEvents");
        this.callbackController.removeCallback("/test/receipt");
    }

}
