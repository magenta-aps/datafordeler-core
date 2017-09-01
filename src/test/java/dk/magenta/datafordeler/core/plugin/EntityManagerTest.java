package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.EntityReference;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.testutil.CallbackController;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.core.testutil.KeyExpectorCallback;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.plugindemo.DemoEntityManager;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityReference;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistrationReference;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;


import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lars on 15-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class EntityManagerTest extends PluginTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected CallbackController callbackController;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @LocalServerPort
    private int port;

    @Test
    public void testLinks() {
        EntityManager entityManager = this.plugin.getEntityManager(DemoEntity.schema);
        Assert.assertTrue(entityManager.getRegisterManager() instanceof DemoRegisterManager);
        Assert.assertEquals(entityManager, entityManager.getRegisterManager().getEntityManager(DemoEntity.schema));
    }

    @Test
    public void testManaged() {
        EntityManager entityManager = this.plugin.getEntityManager(DemoEntity.schema);
        Assert.assertEquals(DemoEntity.class, entityManager.getManagedEntityClass());
        Assert.assertEquals(DemoEntityReference.class, entityManager.getManagedEntityReferenceClass());
        Assert.assertEquals(DemoRegistration.class, entityManager.getManagedRegistrationClass());
        Assert.assertEquals(DemoRegistrationReference.class, entityManager.getManagedRegistrationReferenceClass());
        Assert.assertEquals(DemoEntity.schema, entityManager.getSchema());
    }

    @Test
    public void testReceipts() throws IOException {
        DemoEntityManager entityManager = (DemoEntityManager) this.plugin.getEntityManager(DemoEntity.schema);
        Receipt receipt1 = new Receipt("Testing receipt 1", OffsetDateTime.now());
        Receipt receipt2 = new Receipt("Testing receipt 2", OffsetDateTime.now());
        ExpectorCallback receiptResponder = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/receipt", "response body", receiptResponder);
        try {
            entityManager.setBaseEndpoint(new URI("http", null, "localhost", this.port, "/test", null, null));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ArrayList<Receipt> receipts = new ArrayList<>();
        receipts.add(receipt1);
        receipts.add(receipt2);
        Map<Receipt, Integer> responses = entityManager.sendReceipts(receipts);
        Assert.assertNotNull(responses.get(receipt1));
        Assert.assertNotNull(responses.get(receipt2));
        Assert.assertEquals(200, responses.get(receipt1).intValue());
        Assert.assertEquals(200, responses.get(receipt2).intValue());
    }


    @Test
    public void testFetchRegistration() throws IOException, DataFordelerException, URISyntaxException {
        DemoEntityManager entityManager = (DemoEntityManager) this.plugin.getEntityManager(DemoEntity.schema);

        String checksum = this.hash(UUID.randomUUID().toString());
        String uuid = UUID.randomUUID().toString();
        RegistrationReference reference = new DemoRegistrationReference(new URI("http",null, "localhost", this.port, "/test/get/"+checksum, null, null));

        String template = this.getPayload("/referencelookuptest.json");
        String full = template
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        ExpectorCallback lookupCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/get/" + checksum, full, lookupCallback);

        List<? extends Registration> registrations = entityManager.fetchRegistration(reference);
        Assert.assertNotNull(registrations);
        Assert.assertFalse(registrations.isEmpty());
        Registration registration = registrations.get(0);
        Assert.assertNotNull(registration);
        Assert.assertEquals(checksum, registration.getRegisterChecksum());
        Assert.assertEquals(uuid, registration.getEntity().getUUID().toString());
    }


    @Test
    public void testFetchRegistrationFail1() throws IOException, DataFordelerException, URISyntaxException {
        DemoEntityManager entityManager = (DemoEntityManager) this.plugin.getEntityManager(DemoEntity.schema);

        String checksum = this.hash(UUID.randomUUID().toString());
        String uuid = UUID.randomUUID().toString();
        RegistrationReference reference = new DemoRegistrationReference(new URI("http",null, "localhost", Application.servicePort, "/test/get/"+checksum, null, null));

        String template = this.getPayload("/referencelookuptest.json");
        String full = template
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        String otherEndpoint = "/test/get/" + UUID.randomUUID().toString();
        ExpectorCallback lookupCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse(otherEndpoint, full, lookupCallback);

        exception.expect(FailedReferenceException.class);
        List<? extends Registration> registration = entityManager.fetchRegistration(reference);


        this.callbackController.removeCallback(otherEndpoint);
    }

    public class OtherRegistrationReference implements RegistrationReference {

        private String checksum;
        private URI uri;

        public OtherRegistrationReference(URI uri) {
            this.uri = uri;
        }

        @Override
        public String getChecksum() {
            return null;
        }

        @Override
        public URI getURI() {
            return null;
        }
    }

    @Test
    public void testFetchRegistrationFail2() throws IOException, DataFordelerException, URISyntaxException {
        DemoEntityManager entityManager = (DemoEntityManager) this.plugin.getEntityManager(DemoEntity.schema);

        String checksum = this.hash(UUID.randomUUID().toString());
        String uuid = UUID.randomUUID().toString();
        RegistrationReference reference = new OtherRegistrationReference(new URI("http",null, "localhost", Application.servicePort, "/test/get/"+checksum, null, null));

        String template = this.getPayload("/referencelookuptest.json");
        String full = template
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        ExpectorCallback lookupCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/get/" + UUID.randomUUID().toString(), full, lookupCallback);

        exception.expect(WrongSubclassException.class);
        List<? extends Registration> registration = entityManager.fetchRegistration(reference);
    }


    @Test
    public void listChecksumsByTimestampTest() throws DataFordelerException, IOException {
        EntityManager entityManager = this.plugin.getEntityManager(DemoEntity.schema);

        KeyExpectorCallback listChecksumsCallback = new KeyExpectorCallback("from", "2017-01-01");
        String response = this.getPayload("/listchecksums_date_response.json");
        this.callbackController.addCallbackResponse("/test/listChecksums", response, listChecksumsCallback);

        for (OffsetDateTime time : new OffsetDateTime[]{null, OffsetDateTime.parse("2017-01-01T00:00:00Z")}) {
            ItemInputStream<? extends EntityReference> itemStream = entityManager.listRegisterChecksums(time);
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
    public void testExpandBaseUri() throws URISyntaxException {
        Assert.assertEquals(new URI("http://localhost/foo/bar"), EntityManager.expandBaseURI(new URI("http://localhost"), "/foo/bar"));
        Assert.assertEquals(new URI("http://localhost/foo/bar?bat=42#hey"), EntityManager.expandBaseURI(new URI("http://localhost"), "/foo/bar", "bat=42", "hey"));
    }

}
