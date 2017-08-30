package dk.magenta.datafordeler.plugindemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.exception.WrongSubclassException;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.plugin.Communicator;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.HttpCommunicator;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.plugindemo.fapi.helloworld.v1.DemoEntityService;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityReference;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistrationReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 13-03-17.
 */
@Component
public class DemoEntityManager extends EntityManager {

    protected Logger log = LogManager.getLogger("DemoEntityManager");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DemoEntityService demoEntityService;

    private HttpCommunicator commonFetcher;

    private String[] URISubstrings = {
            "http://localhost:" + TestConfig.servicePort
    };

    private URI baseEndpoint = null;

    public DemoEntityManager() {
        this.managedEntityClass = DemoEntity.class;
        this.managedEntityReferenceClass = DemoEntityReference.class;
        this.managedRegistrationReferenceClass = DemoRegistrationReference.class;
        this.managedRegistrationClass = DemoRegistration.class;
        this.commonFetcher = new HttpCommunicator();
        try {
            this.baseEndpoint = new URI("http", null, "localhost", TestConfig.servicePort, "/test", null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Collection<String> getHandledURISubstrings() {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, this.URISubstrings);
        return list;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected Communicator getRegistrationFetcher() {
        return this.commonFetcher;
    }

    @Override
    protected Communicator getReceiptSender() {
        return this.commonFetcher;
    }

    @Override
    public FapiService getEntityService() {
        return this.demoEntityService;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }


    /**
     * Get the base URI for the register; other EndPoint methods will call this to construct their own return values
     * @return
     */
    @Override
    public URI getBaseEndpoint() {
        return this.baseEndpoint;
    }

    @Override
    public String getSchema() {
        return DemoEntity.schema;
    }

    public void setBaseEndpoint(URI baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
    }

    /** Receipt sending **/

    @Override
    protected URI getReceiptEndpoint(Receipt receipt) {
        return expandBaseURI(this.getBaseEndpoint(), "/receipt");
    }


    /** Reference parsing **/

    public RegistrationReference parseReference(InputStream referenceData) throws IOException {
        return this.objectMapper.readValue(referenceData, this.managedRegistrationReferenceClass);
    }

    public RegistrationReference parseReference(String referenceData, String charsetName) throws IOException {
        return this.objectMapper.readValue(referenceData.getBytes(charsetName), this.managedRegistrationReferenceClass);
    }

    public RegistrationReference parseReference(URI referenceURI) {
        return new DemoRegistrationReference(referenceURI);
    }

    /** Registration parsing **/

    public List<Registration> parseRegistration(InputStream registrationData) throws DataFordelerException {
        String data = new Scanner(registrationData,"UTF-8").useDelimiter("\\A").next();
        return this.parseRegistration(data, "utf-8");
        // return this.objectMapper.readValue(registrationData, this.managedRegistrationClass);
    }

    @Override
    public List<Registration> parseRegistration(JsonNode jsonNode) throws ParseException {
        try {
            return Collections.singletonList(this.objectMapper.treeToValue(jsonNode, this.managedRegistrationClass));
        } catch (JsonProcessingException e) {
            throw new ParseException(e.getMessage());
        }
    }

    public List<Registration> parseRegistration(String registrationData, String charsetName) throws DataFordelerException {
        this.getLog().info("Parsing registration data");
        try {
            return Collections.singletonList(this.objectMapper.readValue(registrationData.getBytes(charsetName), this.managedRegistrationClass));
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    /** Registration fetching **/

    public URI getRegistrationInterface(RegistrationReference reference) throws WrongSubclassException {
        if (!this.managedRegistrationReferenceClass.isInstance(reference)) {
            throw new WrongSubclassException(this.managedRegistrationReferenceClass, reference);
        }
        if (reference.getURI() != null) {
            return reference.getURI();
        }
        return EntityManager.expandBaseURI(this.getBaseEndpoint(), "/get/"+reference.getChecksum());
    }

    /** Checksum fetching **/

    @Override
    protected URI getListChecksumInterface(OffsetDateTime fromDate) {
        return this.getRegisterManager().getListChecksumInterface(this.getSchema(), fromDate);
    }

    @Override
    protected ItemInputStream<DemoEntityReference> parseChecksumResponse(InputStream responseContent) throws DataFordelerException {
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(responseContent, baos);
            byte[] bytes = baos.toByteArray();
            responseContent = new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return ItemInputStream.parseJsonStream(responseContent, DemoEntityReference.class, "items", this.objectMapper);
    }

}
