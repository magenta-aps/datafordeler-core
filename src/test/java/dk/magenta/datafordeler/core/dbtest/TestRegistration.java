package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.model.Registration;

import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_registration")
public class TestRegistration extends Registration<TestIdentification, TestEntity, TestRegistration, TestEffect> {

    public TestRegistration(TestEntity entity, OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        super(entity, registrationFrom, registrationTo);
    }
    public TestRegistration(TestEntity entity, TemporalAccessor registrationFrom, TemporalAccessor registrationTo) {
        super(entity, registrationFrom, registrationTo);
    }
    public TestRegistration(TestEntity entity, String registrationFrom, String registrationTo) {
        super(entity, registrationFrom, registrationTo);
    }
}
