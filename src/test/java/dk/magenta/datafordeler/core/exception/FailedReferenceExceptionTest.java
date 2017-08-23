package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistrationReference;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class FailedReferenceExceptionTest {

    @Test
    public void testFailedReferenceException() throws Exception {
        RegistrationReference reference = new DemoRegistrationReference();
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "Not found");

        FailedReferenceException exception1 = new FailedReferenceException(reference, statusLine);
        Assert.assertEquals(reference, exception1.getReference());
        Assert.assertEquals(statusLine, exception1.getStatusLine());
        Assert.assertEquals("datafordeler.import.reference_failed", exception1.getCode());

        URI uri = new URI("https://data.gl/foobar");
        HttpStatusException cause = new HttpStatusException(statusLine, uri);
        FailedReferenceException exception2 = new FailedReferenceException(reference, cause);
        Assert.assertEquals(reference, exception2.getReference());
        Assert.assertEquals(cause, exception2.getCause());
        Assert.assertEquals("datafordeler.import.reference_failed", exception2.getCode());
    }

}
