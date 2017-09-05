package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import dk.magenta.datafordeler.core.testutil.TestUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.user.UserProfile;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import dk.magenta.datafordeler.plugindemo.model.DemoData;
import dk.magenta.datafordeler.plugindemo.model.DemoEffect;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Created by lars on 20-04-17.
 */
@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FapiTest {

    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private QueryManager queryManager;

    @Autowired
    private ObjectMapper objectMapper;

    private SOAPConnectionFactory soapConnectionFactory;
    private SOAPConnection soapConnection ;
    private MessageFactory messageFactory;
    private SOAPMessage soapMessage;
    private SOAPPart soapPart;
    private SOAPEnvelope soapEnvelope;

    @Test
    @Order(order=1)
    public void findDemoPluginTest() {
        String testSchema = DemoEntity.schema;
        Plugin foundPlugin = this.pluginManager.getPluginForSchema(testSchema);
        Assert.assertEquals(DemoPlugin.class, foundPlugin.getClass());
    }

    @Test
    @Order(order=2)
    public void soapExistsTest() throws IOException {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/soap?wsdl", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    @Order(order=3)
    public void restExistsTest() throws IOException {
        this.setupUser();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest/search", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    @Order(order=4)
    public void soapFailOnInvalidUUIDTest() throws IOException, SOAPException {
        this.setupSoap();
        String service = "http://v1.helloworld.fapi.plugindemo.datafordeler.magenta.dk/";
        soapEnvelope.addNamespaceDeclaration("v1", service);
        SOAPBody soapBody = soapEnvelope.getBody();
        QName bodyName = new QName(service,"get", "v1");
        SOAPBodyElement bodyElement = soapBody.addBodyElement(bodyName);
        QName n = new QName(service,"id");
        bodyElement.addChildElement(n).addTextNode("invalid-uuid");
        soapMessage.saveChanges();

        URI soapEndpoint = this.restTemplate.getRestTemplate().getUriTemplateHandler().expand("/demo/postnummer/1/soap");
        try {
            SOAPMessage soapResponseMessage = soapConnection.call(soapMessage, soapEndpoint);
            Assert.fail("Must throw SOAPException on invalid request");
        } catch (SOAPException e) {
        }
    }


    @Test
    @Order(order=5)
    public void restFailOnInvalidUUIDTest() throws IOException {
        this.setupUser();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest/invalid-uuid", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(400, resp.getStatusCode().value());
    }


    // Disabled for now, as we might not need SOAP: @Test
    @Order(order=6)
    public void soapLookupXMLByUUIDTest() throws IOException, SOAPException, DataFordelerException {
        this.setupSoap();
        UUID uuid = this.addTestObject();
        try {
            String service = "http://v1.helloworld.fapi.plugindemo.datafordeler.magenta.dk/";
            soapEnvelope.addNamespaceDeclaration("v1", service);
            SOAPBody soapBody = soapEnvelope.getBody();
            QName bodyName = new QName(service, "get", "v1");
            SOAPBodyElement bodyElement = soapBody.addBodyElement(bodyName);
            QName n = new QName("id");
            bodyElement.addChildElement(n).addTextNode(uuid.toString());
            soapMessage.saveChanges();

            URL soapEndpoint = this.restTemplate.getRestTemplate().getUriTemplateHandler().expand("/demo/postnummer/1/soap").toURL();
            SOAPMessage soapResponseMessage = soapConnection.call(soapMessage, soapEndpoint);
            Assert.assertNotNull(soapResponseMessage);

            System.out.println("soapResponseMessage ("+soapResponseMessage.getClass().getCanonicalName()+"):");
            Document document = soapResponseMessage.getSOAPBody().extractContentAsDocument();
            System.out.println(document.getChildNodes().item(0));

            XPath xpath = XPathFactory.newInstance().newXPath();
            SOAPBody responseBody = soapResponseMessage.getSOAPBody();
            System.out.println("responseBody: "+responseBody);

            try {
                Assert.assertEquals(uuid.toString(), xpath.compile("//return/UUID").evaluate(document, XPathConstants.STRING));
                Assert.assertEquals("fapitest", xpath.compile("//return/domain").evaluate(document, XPathConstants.STRING));

                this.testSoapResponse(document, "postnr", "8000", "2017-02-21T16:02:50+01:00", "2017-05-01T16:06:22+02:00", "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
                this.testSoapResponse(document, "bynavn", "Århus C", "2017-02-21T16:02:50+01:00", "2017-05-01T16:06:22+02:00", "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");

                this.testSoapResponse(document, "postnr", "8000", "2017-02-21T16:02:50+01:00", "2017-05-01T16:06:22+02:00", "2018-01-01T00:00:00+01:00", null);
                this.testSoapResponse(document, "bynavn", "AArhus C", "2017-02-21T16:02:50+01:00", "2017-05-01T16:06:22+02:00", "2018-01-01T00:00:00+01:00", null);

                this.testSoapResponse(document, "postnr", "8000", "2017-05-01T16:06:22+02:00", null, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
                this.testSoapResponse(document, "bynavn", "Århus C", "2017-05-01T16:06:22+02:00", null, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");

                this.testSoapResponse(document, "postnr", "8000", "2017-05-01T16:06:22+02:00", null, "2018-01-01T00:00:00+01:00", null);
                this.testSoapResponse(document, "bynavn", "Aarhus C", "2017-05-01T16:06:22+02:00", null, "2018-01-01T00:00:00+01:00", null);

            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        } finally {
            this.removeTestObject(uuid);
        }
    }

    private void testSoapResponse(Object responseBody, String key, String expected, String registrationFrom, String registrationTo, String effectFrom, String effectTo) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        StringBuilder sb = new StringBuilder();
        sb.append("//return/registration");
        if (registrationFrom != null) {
            sb.append("/registrationFrom[contains(text(), \""+registrationFrom+"\") or contains(text(), \""+toUTC(registrationFrom)+"\")]/..");
        }
        if (registrationTo != null) {
            sb.append("/registrationTo[contains(text(), \""+registrationTo+"\") or contains(text(), \"" + toUTC(registrationTo) + "\")]/..");
        }
        sb.append("/effect");
        if (effectFrom != null) {
            sb.append("/effectFrom[contains(text(),\""+effectFrom+"\") or contains(text(), \"" + toUTC(effectFrom) + "\")]/..");
        }
        if (effectTo != null) {
            sb.append("/effectTo[contains(text(),\""+effectTo+"\") or contains(text(), \"" + toUTC(effectTo) + "\")]/..");
        }
        sb.append("/data/entry/key[contains(text(),\""+key+"\")]/../value");
        Assert.assertEquals(expected, xpath.compile(sb.toString()).evaluate(responseBody, XPathConstants.STRING));
    }

    private String toUTC(String time) {
        return OffsetDateTime.parse(time).atZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Test
    @Order(order=7)
    public void restLookupJSONByUUIDTest() throws IOException, DataFordelerException {
        this.setupUser();
        UUID uuid = this.addTestObject();
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
            ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest/" + uuid.toString(), HttpMethod.GET, httpEntity, String.class);
            Assert.assertEquals(200, resp.getStatusCode().value());
            JsonNode jsonBody = objectMapper.readTree(resp.getBody());


            Assert.assertNotNull(jsonBody);

            Assert.assertEquals("fapitest", jsonBody.findValue("domaene").asText());
            Assert.assertEquals(uuid.toString(), jsonBody.findValue("uuid").asText());
            JsonNode registrations = jsonBody.get("results").get(0).get("registreringer");

            System.out.println("registrations: " + registrations);

            Assert.assertTrue(registrations.isArray());
            Assert.assertEquals(2, registrations.size());

            JsonNode registration1 = registrations.get(0);
            Assert.assertNotNull(registration1);

            Assert.assertEquals(1, registration1.get("sekvensnummer").asInt());
            Assert.assertTrue(OffsetDateTime.parse("2017-02-21T16:02:50+01:00").isEqual(OffsetDateTime.parse(registration1.get("registreringFra").asText())));

            JsonNode registration2 = registrations.get(1);
            Assert.assertNotNull(registration2);
            Assert.assertEquals(2, registration2.get("sekvensnummer").asInt());
            Assert.assertTrue(OffsetDateTime.parse("2017-05-01T16:06:22+02:00").isEqual(OffsetDateTime.parse(registration2.get("registreringFra").asText())));
            Assert.assertTrue(registration2.get("registreringTil").isNull());

            // Restrict on registrationFrom
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2}}, "2017-06-01T00:00:00+00:00", null, null, null);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2, 2}}, "2017-05-01T15:06:22+01:00", null, null, null);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2, 2}}, "2017-05-01T15:06:21+01:00", null, null, null);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2, 2}}, "2017-05-01T15:06:21+01:00", "2017-05-01T15:06:23+01:00", null, null);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2}}, "2017-05-15T00:00:00+01:00", "2017-06-01T00:00:00+01:00", null, null);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2, 2}}, "2017-05-01T15:06:21+01:00", null, "2016-06-01T00:00:00+01:00", "2019-06-01T00:00:00+01:00");
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{1, 1}}, "2017-05-01T15:06:21+01:00", null, "2017-06-01T00:00:00+01:00", "2017-07-01T00:00:00+01:00");
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{0, 0}}, "2017-05-01T15:06:21+01:00", null, "2014-06-01T00:00:00+01:00", "2015-06-01T00:00:00+01:00");
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2}}, null, "2017-04-01T15:06:21+01:00", "2016-06-01T00:00:00+01:00", "2019-06-01T00:00:00+01:00");
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{1}}, null, "2017-04-01T15:06:21+01:00", "2016-06-01T00:00:00+01:00", "2017-06-01T00:00:00+01:00");
        } finally {
            this.removeTestObject(uuid);
        }
    }

    @Test
    @Order(order=8)
    public void soapLookupXMLByParametersTest() throws IOException, SOAPException, DataFordelerException {
        this.setupSoap();
        UUID uuid = this.addTestObject();
        try {
            String service = "http://v1.helloworld.fapi.plugindemo.datafordeler.magenta.dk/";
            soapEnvelope.addNamespaceDeclaration("v1", service);
            SOAPBody soapBody = soapEnvelope.getBody();
            QName bodyName = new QName(service, "search", "v1");
            SOAPBodyElement bodyElement = soapBody.addBodyElement(bodyName);

            SOAPElement queryElement = bodyElement.addChildElement(new QName("query"));

            QName n = new QName("postnr");
            queryElement.addChildElement(n).addTextNode("8000");
            soapMessage.saveChanges();

            URL soapEndpoint = this.restTemplate.getRestTemplate().getUriTemplateHandler().expand("/demo/postnummer/1/soap").toURL();
            SOAPMessage soapResponseMessage = soapConnection.call(soapMessage, soapEndpoint);
            Assert.assertNotNull(soapResponseMessage);

            System.out.println("SOAP response:");
            soapResponseMessage.writeTo(System.out);
            System.out.println("");

            XPath xpath = XPathFactory.newInstance().newXPath();
            SOAPBody responseBody = soapResponseMessage.getSOAPBody();
            System.out.println("responseBody: "+responseBody);
        /*try {
            Assert.assertEquals(uuid.toString(), xpath.compile("//return/UUID").evaluate(responseBody, XPathConstants.STRING));
            Assert.assertEquals("fapitest", xpath.compile("//return/domain").evaluate(responseBody, XPathConstants.STRING));
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }*/
        } finally {
            this.removeTestObject(uuid);
        }
    }


    @Test
    @Order(order=9)
    public void restLookupJSONByParametersTest() throws IOException, DataFordelerException {
        this.setupUser();
        UUID uuid1 = this.addTestObject();
        UUID uuid2 = this.addTestObject();
        try {
            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000", new int[][]{{2, 2}, {2, 2}}, null, null, null, null);
            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000&page=1&pageSize=1", new int[][]{{2, 2}}, null, null, null, null);
            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000&page=2&pageSize=1", new int[][]{{2, 2}}, null, null, null, null);
            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000", new int[][]{{1}, {1}}, "2017-04-01T00:00:00+01:00", "2017-04-01T15:06:21+01:00", "2016-06-01T00:00:00+01:00", "2017-06-01T00:00:00+01:00");
        } finally {
            this.removeTestObject(uuid1);
            this.removeTestObject(uuid2);
        }
    }

    @Test
    @Order(order=10)
    public void restLookupXMLByUUIDTest() throws IOException, DataFordelerException {
        this.setupUser();
        UUID uuid = this.addTestObject();
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/xml");
            HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
            ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest/" + uuid.toString(), HttpMethod.GET, httpEntity, String.class);
            Assert.assertEquals(200, resp.getStatusCode().value());

            String xmlBody = resp.getBody();
            System.out.println(resp.getBody());
            // I know, it's lazy as hell
            Assert.assertTrue(xmlBody.contains(uuid.toString()));
            Assert.assertTrue(xmlBody.contains("fapitest"));
        } finally {
            this.removeTestObject(uuid);
        }
    }


    private void testRegistrationFilter(String urlBase, int[][] expected, String registerFrom, String registerTo, String effectFrom, String effectTo) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept","application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);

        StringBuilder sb = new StringBuilder();
        sb.append(urlBase);
        if (registerFrom != null || registerTo != null || effectFrom != null || effectTo != null) {
            sb.append(urlBase.contains("?") ? "&" : "?");
            StringJoiner sj = new StringJoiner("&");
            if (registerFrom != null) {
                sj.add(FapiService.PARAM_REGISTRATION_FROM[0] + "=" + registerFrom);
            }
            if (registerTo != null) {
                sj.add(FapiService.PARAM_REGISTRATION_TO[0] + "=" + registerTo);
            }
            if (effectFrom != null) {
                sj.add(FapiService.PARAM_EFFECT_FROM[0] + "=" + effectFrom);
            }
            if (effectTo != null) {
                sj.add(FapiService.PARAM_EFFECT_TO[0] + "=" + effectTo);
            }
            sb.append(sj.toString());
        }
        System.out.println("\n------------------------\n"+sb.toString());
        ResponseEntity<String> resp = this.restTemplate.exchange(sb.toString(), HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(200, resp.getStatusCode().value());
        System.out.println(resp.getBody());
        JsonNode jsonBody = objectMapper.readTree(resp.getBody());

        ArrayNode list = (ArrayNode) jsonBody.get("results");
        Assert.assertEquals(expected.length, list.size());
        int i = 0;
        for (JsonNode entity : list) {
            JsonNode registrations = entity.get("registreringer");
            System.out.println(registrations);
            Assert.assertEquals(expected[i].length, registrations.size());
            for (int j = 0; j < expected[i].length; j++) {
                Assert.assertEquals(expected[i][j], registrations.get(j).get("virkninger").size());
            }
            i++;
        }
    }

    private void setupSoap() {
        try {
            //object initialization
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();

            messageFactory = MessageFactory.newInstance();
            soapMessage = messageFactory.createMessage();

            soapPart = soapMessage.getSOAPPart();
            soapEnvelope = soapPart.getEnvelope();

        } catch (UnsupportedOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SOAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private UUID addTestObject() throws DataFordelerException {
        UUID uuid = UUID.randomUUID();
        DemoEntity demoEntity = new DemoEntity();
        demoEntity.setUUID(uuid);
        demoEntity.setDomain("fapitest");


        DemoRegistration demoRegistration = new DemoRegistration();
        demoRegistration.setRegistrationFrom(OffsetDateTime.parse("2017-02-21T16:02:50+01:00"));
        demoRegistration.setRegistrationTo(OffsetDateTime.parse("2017-05-01T15:06:22+01:00"));
        demoRegistration.setRegisterChecksum(UUID.randomUUID().toString());
        demoRegistration.setSequenceNumber(1);
        DemoEffect demoEffect = new DemoEffect(demoRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        DemoData demoData = new DemoData(8000, "Århus C");
        demoData.addEffect(demoEffect);
        DemoEffect demoEffect2 = new DemoEffect(demoRegistration, "2018-01-01T00:00:00+01:00", null);
        DemoData demoData2 = new DemoData(8000, "AArhus C");
        demoData2.addEffect(demoEffect2);

        DemoRegistration demoRegistration2 = new DemoRegistration();
        demoRegistration2.setRegistrationFrom(OffsetDateTime.parse("2017-05-01T15:06:22+01:00"));
        demoRegistration2.setRegistrationTo(null);
        demoRegistration2.setRegisterChecksum(UUID.randomUUID().toString());
        demoRegistration2.setSequenceNumber(2);
        DemoEffect demoEffect3 = new DemoEffect(demoRegistration2, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        DemoData demoData3 = new DemoData(8000, "Århus C");
        demoData3.addEffect(demoEffect3);
        DemoEffect demoEffect4 = new DemoEffect(demoRegistration2, "2018-01-01T00:00:00+01:00", null);
        DemoData demoData4 = new DemoData(8000, "Aarhus C");
        demoData4.addEffect(demoEffect4);

        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            queryManager.saveRegistration(session, demoEntity, demoRegistration);
            queryManager.saveRegistration(session, demoEntity, demoRegistration2);
        } finally {
            transaction.commit();
            session.close();
        }
        return uuid;
    }

    private void removeTestObject(UUID uuid) {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        DemoEntity entity = queryManager.getEntity(session, uuid, DemoEntity.class);
        session.delete(entity);
        transaction.commit();
        session.close();
    }


    private void setupUser() {

        UserProfile testUserProfile = new UserProfile("TestProfile", Arrays.asList(
                DemoRolesDefinition.READ_DEMO_ENTITY_ROLE.getRoleName(),
                DemoRolesDefinition.READ_SERVICE_ROLE.getRoleName()
        ));

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.addUserProfile(testUserProfile);
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }
}
