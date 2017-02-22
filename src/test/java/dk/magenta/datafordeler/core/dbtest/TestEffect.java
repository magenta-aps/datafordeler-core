package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.model.Effect;

import javax.persistence.Table;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_effect")
public class TestEffect extends Effect<TestIdentification, TestEntity, TestRegistration, TestEffect, TestData> {

    public TestEffect(TestRegistration registration) {
        super(registration);
    }
}

