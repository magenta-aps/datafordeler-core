package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.fapi.helloworld.v1.DemoEntityService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class)
public class IndexTest {

    @Autowired
    private DemoEntityService entityService;

    @Autowired
    private DemoPlugin plugin;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testServiceDescriptor() {
        String path = "/test";
        ServiceDescriptor serviceDescriptor = entityService.getServiceDescriptor(path, false);
        Assert.assertTrue(serviceDescriptor instanceof RestServiceDescriptor);
        Assert.assertEquals(plugin, serviceDescriptor.getPlugin());
        Assert.assertEquals("rest", serviceDescriptor.getType());
        Assert.assertEquals(path, serviceDescriptor.getServiceAddress());
        Assert.assertEquals(path, serviceDescriptor.getMetaAddress());
        Assert.assertEquals("postnummer", serviceDescriptor.getServiceName());
        HashMap<String, String> mappedFields = new HashMap<>();
        for (ServiceDescriptor.ServiceQueryField queryField : serviceDescriptor.getFields()) {
            mappedFields.put(queryField.name, queryField.type);
        }
        Assert.assertEquals("int", mappedFields.get("postnr"));
        Assert.assertEquals("boolean", mappedFields.get("aktiv"));
        Assert.assertEquals("string", mappedFields.get("bynavn"));
        Assert.assertEquals("string", mappedFields.get("registrationFrom"));
        Assert.assertEquals("string", mappedFields.get("registrationTo"));
        Assert.assertEquals("string", mappedFields.get("effectFrom"));
        Assert.assertEquals("string", mappedFields.get("effectTo"));
        Assert.assertEquals("int", mappedFields.get("pageSize"));
        Assert.assertEquals("int", mappedFields.get("page"));


        serviceDescriptor = entityService.getServiceDescriptor(path, true);
        Assert.assertTrue(serviceDescriptor instanceof SoapServiceDescriptor);
        Assert.assertEquals(plugin, serviceDescriptor.getPlugin());
        Assert.assertEquals("soap", serviceDescriptor.getType());
        Assert.assertEquals(path, serviceDescriptor.getServiceAddress());
        Assert.assertEquals(path, serviceDescriptor.getMetaAddress());
        Assert.assertEquals("postnummer", serviceDescriptor.getServiceName());
        mappedFields = new HashMap<>();
        for (ServiceDescriptor.ServiceQueryField queryField : serviceDescriptor.getFields()) {
            mappedFields.put(queryField.name, queryField.type);
        }
        Assert.assertEquals("int", mappedFields.get("postnr"));
        Assert.assertEquals("boolean", mappedFields.get("aktiv"));
        Assert.assertEquals("string", mappedFields.get("bynavn"));
        Assert.assertEquals("string", mappedFields.get("registrationFrom"));
        Assert.assertEquals("string", mappedFields.get("registrationTo"));
        Assert.assertEquals("string", mappedFields.get("effectFrom"));
        Assert.assertEquals("string", mappedFields.get("effectTo"));
        Assert.assertEquals("int", mappedFields.get("pageSize"));
        Assert.assertEquals("int", mappedFields.get("page"));
    }

    @Test
    public void testIndex() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest", HttpMethod.GET, httpEntity, String.class);
        JsonNode json = objectMapper.readTree(resp.getBody());
        Assert.assertNotNull(json);
        Assert.assertTrue(json instanceof ObjectNode);
        ObjectNode object = (ObjectNode) json;
        Assert.assertEquals("postnummer", object.get("service_name").textValue());
        Assert.assertEquals("/demo/postnummer/1/rest/{UUID}", object.get("fetch_url").textValue());
        Assert.assertEquals("/demo/postnummer/1/rest/search", object.get("search_url").textValue());
        Assert.assertEquals("https://redmine.magenta-aps.dk/projects/dafodoc/wiki/API", object.get("declaration_url").textValue());
        Assert.assertEquals("rest", object.get("type").textValue());
        Assert.assertEquals("/demo/postnummer/1/rest", object.get("metadata_url").textValue());
        ArrayNode queryFields = (ArrayNode) object.get("search_queryfields");
        HashMap<String, String> mappedFields = new HashMap<>();
        Iterator<JsonNode> queryFieldNodes = queryFields.elements();
        while (queryFieldNodes.hasNext()) {
            JsonNode queryFieldNode = queryFieldNodes.next();
            Assert.assertTrue(queryFieldNode instanceof ObjectNode);
            ObjectNode queryFieldObjectNode = (ObjectNode) queryFieldNode;
            mappedFields.put(queryFieldObjectNode.get("name").textValue(), queryFieldObjectNode.get("type").textValue());
        }
        Assert.assertEquals("int", mappedFields.get("postnr"));
        Assert.assertEquals("boolean", mappedFields.get("aktiv"));
        Assert.assertEquals("string", mappedFields.get("bynavn"));
        Assert.assertEquals("string", mappedFields.get("registrationFrom"));
        Assert.assertEquals("string", mappedFields.get("registrationTo"));
        Assert.assertEquals("string", mappedFields.get("effectFrom"));
        Assert.assertEquals("string", mappedFields.get("effectTo"));
        Assert.assertEquals("int", mappedFields.get("pageSize"));
        Assert.assertEquals("int", mappedFields.get("page"));
    }

}
