package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Entity;

import javax.persistence.Table;

/**
 * Created by lars on 12-01-17.
 */
@javax.persistence.Entity
@Table(name="demo_entity")
public class DemoEntity extends Entity<DemoEntity, DemoRegistration> {

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Postnummer";
}
