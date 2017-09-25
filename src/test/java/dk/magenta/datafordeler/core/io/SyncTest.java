package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lars on 10-04-17.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SyncTest extends GapiTestBase {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    Engine engine;

    @LocalServerPort
    private int port;

    @Test
    public void testSynchronize() throws DataFordelerException, IOException {
        Session session = this.sessionManager.getSessionFactory().openSession();
        String uuid1 = "2eed5323-f94f-5a9f-9607-39739c3585b5";
        String uuid2 = "2eed5323-f94f-5a9f-9607-39739c3585b6";
        try {
            Transaction transaction = session.beginTransaction();

            ExpectorCallback listChecksumsCallback = new ExpectorCallback();
            String response = this.getPayload("/listchecksums_response.json");
            this.callbackController.addCallbackResponse("/test/listChecksums", response, listChecksumsCallback);

            HashMap<String, String[]> checksums = new HashMap<>();
            checksums.put(uuid1, new String[]{"8da553a54d4c7b1bbb95f47d803a758d", "2a2ef17de084b10fe30ca726e2168c2e"});
            checksums.put(uuid2, new String[]{"8da553a54d4c7b1bbb95f47d803a758e", "2a2ef17de084b10fe30ca726e2168c2f"});

            for (String entityId : checksums.keySet()) {
                int sequenceNumber = 0;
                for (String registrationChecksum : checksums.get(entityId)) {
                    sequenceNumber++;
                    String full = this.getPayload("/referencelookuptest.json")
                            .replace("%{checksum}", registrationChecksum)
                            .replace("%{entityid}", entityId)
                            .replace("%{sequenceNumber}", Integer.toString(sequenceNumber));
                    ExpectorCallback lookupCallback = new ExpectorCallback();
                    this.callbackController.addCallbackResponse("/test/get/" + registrationChecksum, full, lookupCallback);
                }
            }

            List<? extends Registration> newRegistrations;
            Plugin plugin = pluginManager.getPluginForSchema("Postnummer");
            DemoRegisterManager registerManager = (DemoRegisterManager) plugin.getRegisterManager();
            registerManager.setPort(this.port);
            newRegistrations = engine.synchronize(session, plugin, null);
            Assert.assertEquals(4, newRegistrations.size());
            transaction.commit();


            transaction = session.beginTransaction();
            newRegistrations = engine.synchronize(session, plugin, null);
            transaction.commit();
            Assert.assertEquals(0, newRegistrations.size());
        } finally {
            session.close();
        }

        this.deleteEntity(uuid1);
        this.deleteEntity(uuid2);
    }
}
