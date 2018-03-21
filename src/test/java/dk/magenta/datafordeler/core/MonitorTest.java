package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

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


    @Test
    @Ignore
    public void testPullMonitoring() {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = this.restTemplate.exchange("/monitor/pull", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void testGetTimeBefore() throws ParseException {
        CronExpression expression = new CronExpression("0 0 0 * * ?");
        expression.setTimeZone(TimeZone.getTimeZone("UTC"));
        Assert.assertTrue(
                MonitorService.getTimeBefore(
                        expression,
                        OffsetDateTime.of(2018, 1, 30, 12, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ).equals(
                        OffsetDateTime.of(2018, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                )
        );

        Assert.assertTrue(
                MonitorService.getTimeBefore(
                        expression,
                        OffsetDateTime.of(2018, 1, 30, 0, 0, 1, 0, ZoneOffset.UTC).toInstant()
                ).equals(
                        OffsetDateTime.of(2018, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                )
        );



        expression = new CronExpression("* * * * * ?");
        expression.setTimeZone(TimeZone.getTimeZone("UTC"));
        Assert.assertTrue(
                MonitorService.getTimeBefore(
                        expression,
                        OffsetDateTime.of(2018, 1, 30, 12, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ).equals(
                        OffsetDateTime.of(2018, 1, 30, 11, 59, 59, 0, ZoneOffset.UTC).toInstant()
                )
        );


    }
}
