package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class MissingEntityExceptionTest {

    @Test
    public void testMissingEntityExceptionTest() {
        DemoRegistration registration = new DemoRegistration();
        MissingEntityException exception = new MissingEntityException(registration);

        Assert.assertEquals(registration, exception.getRegistration());
        Assert.assertEquals("datafordeler.import.missing_entity", exception.getCode());
    }

}
