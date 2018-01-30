package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@PropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MonitorTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testDatabaseMonitoring() {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = this.restTemplate.exchange("/monitor/database", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
