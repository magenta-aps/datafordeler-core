package dk.magenta.datafordeler.core.gapi;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.EntityReference;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.core.testutil.KeyExpectorCallback;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.EOFException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by lars on 03-04-17.
 */
@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ListChecksumTest extends GapiTestBase {

    @Autowired
    private PluginManager pluginManager;

    @LocalServerPort
    int port;

    @Before
    public void before() {
        Plugin plugin = this.pluginManager.getPluginForSchema("Postnummer");
        DemoRegisterManager registerManager = (DemoRegisterManager) plugin.getRegisterManager();
        registerManager.setPort(this.port);
    }

    @After
    public void after() {
        Plugin plugin = this.pluginManager.getPluginForSchema("Postnummer");
        DemoRegisterManager registerManager = (DemoRegisterManager) plugin.getRegisterManager();
        registerManager.setPort(Application.servicePort);
    }

    @Test
    @Order(order=1)
    public void listAllChecksumsTest() throws DataFordelerException, IOException {
        Plugin plugin = pluginManager.getPluginForSchema("Postnummer");
        DemoRegisterManager registerManager = (DemoRegisterManager) plugin.getRegisterManager();
        ExpectorCallback listChecksumsCallback = new ExpectorCallback();
        String response = this.getPayload("/listchecksums_response.json");
        this.callbackController.addCallbackResponse("/test/listChecksums", response, listChecksumsCallback);

        ItemInputStream<? extends EntityReference> itemStream = registerManager.listRegisterChecksums(null, OffsetDateTime.now());
        ArrayList<EntityReference> allReferences = new ArrayList<>();
        EntityReference entityReference;
        while ((entityReference = itemStream.next()) != null) {
            allReferences.add(entityReference);
        }
        Assert.assertEquals(2, allReferences.size());

        DemoEntityReference firstReference = (DemoEntityReference) allReferences.get(0);
        Assert.assertEquals(UUID.fromString("2eed5323-f94f-5a9f-9607-39739c3585b5"), firstReference.getObjectId());
        Assert.assertEquals(2, firstReference.getRegistrationReferences().size());
        Assert.assertEquals(1, firstReference.getRegistrationReferences().get(0).getSequenceNumber());
        Assert.assertEquals(2, firstReference.getRegistrationReferences().get(1).getSequenceNumber());
        Assert.assertEquals("8da553a54d4c7b1bbb95f47d803a758d", firstReference.getRegistrationReferences().get(0).getChecksum());
        Assert.assertEquals("2a2ef17de084b10fe30ca726e2168c2e", firstReference.getRegistrationReferences().get(1).getChecksum());

        DemoEntityReference secondReference = (DemoEntityReference) allReferences.get(1);
        Assert.assertEquals(UUID.fromString("2eed5323-f94f-5a9f-9607-39739c3585b6"), secondReference.getObjectId());
        Assert.assertEquals(2, secondReference.getRegistrationReferences().size());
        Assert.assertEquals(1, secondReference.getRegistrationReferences().get(0).getSequenceNumber());
        Assert.assertEquals(2, secondReference.getRegistrationReferences().get(1).getSequenceNumber());
        Assert.assertEquals("8da553a54d4c7b1bbb95f47d803a758e", secondReference.getRegistrationReferences().get(0).getChecksum());
        Assert.assertEquals("2a2ef17de084b10fe30ca726e2168c2f", secondReference.getRegistrationReferences().get(1).getChecksum());

        this.callbackController.removeCallback("/test/listChecksums");

        this.deleteEntity("2eed5323-f94f-5a9f-9607-39739c3585b5");
        this.deleteEntity("2eed5323-f94f-5a9f-9607-39739c3585b6");
    }


    @Test
    @Order(order=2)
    public void listChecksumsByTimestampTest() throws DataFordelerException, IOException {
        Plugin plugin = pluginManager.getPluginForSchema("Postnummer");
        EntityManager entityManager = plugin.getEntityManager("Postnummer");
        KeyExpectorCallback listChecksumsCallback = new KeyExpectorCallback("from", "2017-01-01");
        String response = this.getPayload("/listchecksums_date_response.json");
        this.callbackController.addCallbackResponse("/test/listChecksums", response, listChecksumsCallback);

        ItemInputStream<? extends EntityReference> itemStream = entityManager.listRegisterChecksums(OffsetDateTime.parse("2017-01-01T00:00:00Z"));
        ArrayList<EntityReference> allReferences = new ArrayList<>();
        EntityReference entityReference;
        try {
            while ((entityReference = itemStream.next()) != null) {
                allReferences.add(entityReference);
            }
        } catch (EOFException e) {}

        System.out.println(allReferences);
        Assert.assertEquals(2, allReferences.size());

        DemoEntityReference firstReference = (DemoEntityReference) allReferences.get(0);
        Assert.assertEquals(DemoEntity.schema, firstReference.getType());
        Assert.assertEquals(UUID.fromString("2eed5323-f94f-5a9f-9607-39739c3585b5"), firstReference.getObjectId());
        Assert.assertEquals(1, firstReference.getRegistrationReferences().size());
        Assert.assertEquals(1, firstReference.getRegistrationReferences().get(0).getSequenceNumber());
        Assert.assertEquals("2a2ef17de084b10fe30ca726e2168c2e", firstReference.getRegistrationReferences().get(0).getChecksum());

        DemoEntityReference secondReference = (DemoEntityReference) allReferences.get(1);
        Assert.assertEquals(DemoEntity.schema, secondReference.getType());
        Assert.assertEquals(UUID.fromString("2eed5323-f94f-5a9f-9607-39739c3585b6"), secondReference.getObjectId());
        Assert.assertEquals(1, secondReference.getRegistrationReferences().size());
        Assert.assertEquals(2, secondReference.getRegistrationReferences().get(0).getSequenceNumber());
        Assert.assertEquals("2a2ef17de084b10fe30ca726e2168c2f", secondReference.getRegistrationReferences().get(0).getChecksum());

        this.callbackController.removeCallback("/test/listChecksums");
        this.deleteEntity("2eed5323-f94f-5a9f-9607-39739c3585b5");
        this.deleteEntity("2eed5323-f94f-5a9f-9607-39739c3585b6");
    }
}
