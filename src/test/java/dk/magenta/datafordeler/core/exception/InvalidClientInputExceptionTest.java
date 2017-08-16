package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class InvalidClientInputExceptionTest {

    @Test
    public void testInvalidClientInputException() {
        String message = "invalid input";
        InvalidClientInputException exception = new InvalidClientInputException(message);

        Assert.assertEquals(message, exception.getMessage());
        Assert.assertEquals("datafordeler.http.invalid-client-input", exception.getCode());
    }

}
