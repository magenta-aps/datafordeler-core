package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Identification;

import javax.persistence.Table;
import java.util.UUID;

@javax.persistence.Entity
@Table(name="demo_entity")
public class DemoEntity extends Entity<DemoEntity, DemoRegistration> {

    public DemoEntity() {
    }

    public DemoEntity(Identification identification) {
        super(identification);
    }

    public DemoEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    @Override
    protected DemoRegistration createEmptyRegistration() {
        return new DemoRegistration();
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Postnummer";
}
