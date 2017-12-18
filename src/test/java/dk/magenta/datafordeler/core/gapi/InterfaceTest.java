package dk.magenta.datafordeler.core.gapi;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InterfaceTest extends GapiTestBase {

    /**
     * Tests that the GAPI entry point throws a 405 Method Not Allowed on GET, PUT, DELETE and HEAD
     * @throws IOException
     */
    @Test
    @Order(order=1)
    public void methodNotSupported() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        String body = "";
        HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
        HttpMethod[] methods = {HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.HEAD};
        for (HttpMethod method : methods) {
            ResponseEntity<String> resp = this.restTemplate.exchange(INTERFACE_PATH, method, httpEntity, String.class);
            Assert.assertEquals("Method "+method.name(),405, resp.getStatusCode().value());
        }
    }

    /**
     * Tests that the GAPI entry point sends a 200 OK on OPTIONS
     * @throws IOException
     */
    @Test
    @Order(order=2)
    public void options() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        String body = "";
        HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> resp = this.restTemplate.exchange(INTERFACE_PATH, HttpMethod.OPTIONS, httpEntity, String.class);
        Assert.assertEquals(200, resp.getStatusCode().value());
        Assert.assertTrue("OPTIONS headers should contain POST", resp.getHeaders().getAllow().contains(HttpMethod.POST));
        Assert.assertFalse("OPTIONS headers should not contain GET", resp.getHeaders().getAllow().contains(HttpMethod.GET));
        Assert.assertFalse("OPTIONS headers should not contain PUT", resp.getHeaders().getAllow().contains(HttpMethod.PUT));
        Assert.assertFalse("OPTIONS headers should not contain DELETE", resp.getHeaders().getAllow().contains(HttpMethod.DELETE));
    }

    /**
     * Tests that the GAPI entry point accepts a test input
     * @throws IOException
     */
    @Test
    @Order(order=3)
    public void odata() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        InputStream stream = InterfaceTest.class.getResourceAsStream("/gapitest_request.json");
        String body = IOUtils.toString(stream, StandardCharsets.UTF_8);
        HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> resp = this.restTemplate.exchange("/odata/gapi/Events", HttpMethod.POST, httpEntity, String.class);
        Assert.assertEquals(201, resp.getStatusCode().value());
        this.deleteEntity("e3b59524-e577-4d1c-944c-f81b826a7bfd");
    }

}
