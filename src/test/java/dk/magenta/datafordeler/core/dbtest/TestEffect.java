package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.model.Effect;

import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_effect")
public class TestEffect extends Effect<TestEntity, TestRegistration, TestEffect, TestData> {

    public TestEffect() {}

    public TestEffect(TestRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }
    public TestEffect(TestRegistration registration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        super(registration, effectFrom, effectTo);
    }
    public TestEffect(TestRegistration registration, String effectFrom, String effectTo) {
        super(registration, effectFrom, effectTo);
    }
}

