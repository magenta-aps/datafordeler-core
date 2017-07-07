package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Registration;

import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;

/**
 * Created by lars on 12-01-17.
 */
@javax.persistence.Entity
@Table(name="demo_registration")
public class DemoRegistration extends Registration<DemoEntity, DemoRegistration, DemoEffect> {

    public DemoRegistration() {}

    public DemoRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }
    public DemoRegistration(TemporalAccessor registrationFrom, TemporalAccessor registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }
    public DemoRegistration(String registrationFrom, String registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    // Should match the field names on the register; change as required
    @JsonProperty(value = "registerFra")
    public void setRegistreringFra(OffsetDateTime from) {
        this.registreringFra = from;
    }

    @JsonProperty(value = "registerTil")
    public void setRegistreringTil(OffsetDateTime to) {
        this.registreringTil = to;
    }

    @JsonProperty(value = "virkninger")
    public void setVirkninger(Collection<DemoEffect> virkninger) {
        super.setVirkninger(virkninger);
    }

}
