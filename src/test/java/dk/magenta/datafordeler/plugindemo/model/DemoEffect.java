package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;

import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
