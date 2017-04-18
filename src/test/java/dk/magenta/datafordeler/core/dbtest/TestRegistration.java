package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.database.Registration;

import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_registration")
public class TestRegistration extends Registration<TestEntity, TestRegistration, TestEffect> {

    public TestRegistration() {}

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
