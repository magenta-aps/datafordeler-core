package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class MismatchingRegistrationBoundaryExceptionTest {

    @Test
    public void testMismatchingRegistrationBoundaryException() {
        OffsetDateTime from1 = OffsetDateTime.parse("2017-07-25T09:02:27Z");
        OffsetDateTime to1 = OffsetDateTime.parse("2017-12-31T23:59:59Z");
        String checksum1 = UUID.randomUUID().toString();
        DemoRegistration registration1 = new DemoRegistration(from1, to1, 1);
        registration1.setRegisterChecksum(checksum1);

        OffsetDateTime from2 = OffsetDateTime.parse("2018-01-01T00:00:00Z");
        OffsetDateTime to2 = OffsetDateTime.parse("2019-12-31T23:59:59Z");
        String checksum2 = UUID.randomUUID().toString();
        DemoRegistration registration2 = new DemoRegistration(from2, to2, 2);
        registration2.setRegisterChecksum(checksum2);

        MismatchingRegistrationBoundaryException exception = new MismatchingRegistrationBoundaryException(registration2, registration1);

        Assert.assertEquals("Mismatching timestamps; incoming registration " + checksum2 + " with registrationFromBefore 2018-01-01T00:00:00Z does not match existing registration " + checksum1 + " with registrationToBefore 2017-12-31T23:59:59Z", exception.getMessage());
        Assert.assertEquals(registration1, exception.getExistingRegistration());
        Assert.assertEquals(registration2, exception.getNewRegistration());
        Assert.assertEquals("datafordeler.import.registration_boundary_mismatch", exception.getCode());
    }

}
