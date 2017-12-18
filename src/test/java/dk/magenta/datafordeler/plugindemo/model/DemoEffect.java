package dk.magenta.datafordeler.plugindemo.model;

import dk.magenta.datafordeler.core.database.Effect;

import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

@javax.persistence.Entity
@Table(name="demo_effect")
public class DemoEffect extends Effect<DemoRegistration, DemoEffect, DemoData> {

    public DemoEffect() {
        super();
    }

    public DemoEffect(DemoRegistration testRegistration, OffsetDateTime virkingFra, OffsetDateTime virkingTil) {
        super(testRegistration, virkingFra, virkingTil);
    }
    public DemoEffect(DemoRegistration testRegistration, TemporalAccessor virkingFra, TemporalAccessor virkingTil) {
        super(testRegistration, virkingFra, virkingTil);
    }
    public DemoEffect(DemoRegistration testRegistration, String virkingFra, String virkingTil) {
        super(testRegistration, virkingFra, virkingTil);
    }

}
