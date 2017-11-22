package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class SkippedSequenceNumberExceptionTest {

    @Test
    public void testSkippedSequenceNumberException() {
        DemoRegistration registration = new DemoRegistration(OffsetDateTime.parse("2017-07-25T09:25:33Z"), OffsetDateTime.parse("2018-01-01T00:00:00Z"), 3);
        SkippedSequenceNumberException exception = new SkippedSequenceNumberException(registration, 1);

        Assert.assertEquals("Sequencenumber 3 not matching existing highest sequencenumber 1; must be exactly one higher", exception.getMessage());
        Assert.assertEquals(registration, exception.getRegistration());
        Assert.assertEquals(1, exception.getHighestSequenceNumber());
        Assert.assertEquals("datafordeler.import.skipped_sequence_number", exception.getCode());
    }

}
