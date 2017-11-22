package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lars on 03-04-17.
 */
public class DemoEntityReference extends EntityReference<DemoEntity, DemoRegistrationReference> implements Serializable {

    @JsonProperty
    private String type;

    public String getType() {
        return this.type;
    }

    public String toString() {
        return "DemoEntityReference[type="+this.type+",objectId="+this.objectId+",registrations="+this.registrationReferences+"]";
    }

    @JsonProperty("objectID")
    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
    }

    @JsonProperty("registreringer")
    public void setRegistrations(List<DemoRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<DemoRegistrationReference>(registrations);
    }

    public Class<DemoEntity> getEntityClass() {
        return DemoEntity.class;
    }

}
