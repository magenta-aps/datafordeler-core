package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class DuplicateSequenceNumberExceptionTest {

    @Test
    public void testDuplicateSequenceNumberException() {
        DemoRegistration registration1 = new DemoRegistration();
        registration1.setRegisterChecksum(UUID.randomUUID().toString());
        DemoRegistration registration2 = new DemoRegistration();
        registration2.setRegisterChecksum(UUID.randomUUID().toString());
        DuplicateSequenceNumberException exception = new DuplicateSequenceNumberException(registration2, registration1);

        Assert.assertEquals(
                "Duplicate sequencenumber 0, shared between existing registration "+registration1.getRegisterChecksum()+" and new registration "+registration2.getRegisterChecksum(),
                exception.getMessage()
        );
        Assert.assertEquals(registration1, exception.getExistingRegistration());
        Assert.assertEquals(registration2, exception.getNewRegistration());
        Assert.assertEquals("datafordeler.import.duplicate_sequence_number", exception.getCode());
    }

}
