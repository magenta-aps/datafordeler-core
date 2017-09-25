package dk.magenta.datafordeler.core.gapi;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.testutil.CallbackController;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


/**
 * Created by lars on 10-02-17.
 */
// @RunWith(SpringJUnit4ClassRunner.class)
public abstract class GapiTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected CallbackController callbackController;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private QueryManager queryManager;

    protected static final String INTERFACE_PATH = "/odata/gapi/Events";

    protected String envelopData(String schema, String data) throws IOException {
        String template = IOUtils.toString(GapiTestBase.class.getResourceAsStream("/gapitest_dataenvelope.json"), StandardCharsets.UTF_8);
        return template.replace("%{skema}", schema).replace("%{data}", data.replace("\"", "\\\""));
    }

    protected String envelopReference(String schema, String reference) throws IOException {
        String template = IOUtils.toString(GapiTestBase.class.getResourceAsStream("/gapitest_referenceenvelope.json"), StandardCharsets.UTF_8);
        return template.replace("%{skema}", schema).replace("%{reference}", reference.replace("\"", "\\\""));
    }

    protected HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected String getPayload(String resourceName) throws IOException {
        return IOUtils.toString(GapiTestBase.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8);
    }

    protected String getBody(String schema, String resourceName) throws IOException {
        return this.envelopData(schema, this.getPayload(resourceName));
    }

    protected String hash(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void deleteEntity(UUID uuid) {
        this.deleteEntity(uuid.toString());
    }

    protected void deleteEntity(String uuid) {
        Session session = this.sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            DemoEntity entity = this.queryManager.getEntity(session, UUID.fromString(uuid), DemoEntity.class);
            if (entity != null) {
                session.delete(entity);
            }
            transaction.commit();
        } finally {
            session.close();
        }
    }

}
