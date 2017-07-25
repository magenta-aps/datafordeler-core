package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class AccessDeniedExceptionTest {

    @Test
    public void testAccessDeniedException() {
      String message = "You need the red key to open this door";
      AccessDeniedException exception = new AccessDeniedException(message);
      Assert.assertEquals(message, exception.getMessage());
      Assert.assertEquals("datafordeler.accessdenied", exception.getCode());
  }

}
