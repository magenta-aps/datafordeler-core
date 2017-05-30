package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Registration;

import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Collection;

/**
 * Created by lars on 12-01-17.
 */
@javax.persistence.Entity
@Table(name="demo_registration")
public class DemoRegistration extends Registration<DemoEntity, DemoRegistration, DemoEffect> {

    // Should match the field names on the register; change as required
    @JsonProperty(value = "registerFrom")
    public void setRegistrationFrom(OffsetDateTime from) {
        this.registrationFrom = from;
    }

    @JsonProperty(value = "registerTo")
    public void setRegistrationTo(OffsetDateTime to) {
        this.registrationTo = to;
    }

    @JsonProperty(value = "virkninger")
    public void setEffects(Collection<DemoEffect> effects) {
        super.setEffects(effects);
    }
}
