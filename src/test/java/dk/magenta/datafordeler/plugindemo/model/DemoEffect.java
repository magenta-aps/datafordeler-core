package dk.magenta.datafordeler.plugindemo.model;

import dk.magenta.datafordeler.core.database.Effect;

import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="demo_effect")
public class DemoEffect extends Effect<DemoRegistration, DemoEffect, DemoData> {

    public DemoEffect() {
        super();
    }

    public DemoEffect(DemoRegistration testRegistration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(testRegistration, effectFrom, effectTo);
    }
    public DemoEffect(DemoRegistration testRegistration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        super(testRegistration, effectFrom, effectTo);
    }
    public DemoEffect(DemoRegistration testRegistration, String effectFrom, String effectTo) {
        super(testRegistration, effectFrom, effectTo);
    }
}
