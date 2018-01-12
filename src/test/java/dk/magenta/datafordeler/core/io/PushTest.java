package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PushTest extends GapiTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private DemoRegisterManager demoRegisterManager;

    @Before
    public void before() {
        demoRegisterManager.setPort(this.port);
    }

    @After
    public void after() {
        demoRegisterManager.setPort(Application.servicePort);
    }

    /**
     * Tests that an event can be sent to the GAPI interface, the referenced object gets fetched, and a receipt is sent back
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    public void referenceLookupTest() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String checksum = this.hash(UUID.randomUUID().toString());
        String reference = "http://localhost:"+this.port+"/test/get/"+checksum;
        String uuid = UUID.randomUUID().toString();
        String full = this.getPayload("/referencelookuptest.json")
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        ExpectorCallback lookupCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/get/"+checksum, full, lookupCallback);

        ExpectorCallback receiptCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/receipt", "", receiptCallback);

        String body = this.envelopReference("Postnummer", reference);
        HttpEntity<String> httpEntity = new HttpEntity<String>(body, this.getHeaders());
        System.out.println(body);
        ResponseEntity<String> resp = this.restTemplate.exchange("/odata/gapi/Events", HttpMethod.POST, httpEntity, String.class);
        Assert.assertEquals(201, resp.getStatusCode().value());
        Assert.assertTrue(lookupCallback.get(20, TimeUnit.SECONDS));
        Assert.assertTrue(receiptCallback.get(20, TimeUnit.SECONDS));

        this.deleteEntity(uuid);
    }
}
