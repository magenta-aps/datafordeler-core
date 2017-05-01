package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.fapi.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
@Embeddable
public abstract class Entity<E extends Entity, R extends Registration> extends DatabaseEntry {

    public static String getSchema() {
        return "Entity";
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @XmlTransient
    protected Identification identification;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entity")
    protected Set<R> registrations;

    @Transient
    @JsonIgnore
    @XmlTransient
    private Query filter = null;

    public Entity() {
        this.registrations = new HashSet<R>();
        this.identification = new Identification();
    }

    public Entity(Identification identification) {
        this();
        this.identification = identification;
    }

    public Entity(UUID uuid, String domain) {
        this(new Identification(uuid, domain));
    }

    @JsonIgnore
    @XmlTransient
    public Identification getIdentification() {
        return this.identification;
    }

    @JsonProperty("uuid")
    public UUID getUUID() {
        return this.identification.getUuid();
    }

    @JsonProperty("uuid")
    public void setUUID(UUID uuid) {
        this.identification.setUuid(uuid);
    }

    public String getDomain() {
        return this.identification.getDomain();
    }

    @JsonProperty
    public void setDomain(String domain) {
        this.identification.setDomain(domain);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Filter(name = "registrationFromFilter", condition="registrationFrom >= :registrationFromDate")
    public List<R> getRegistrations() {
        if (this.filter != null && (this.filter.getRegistrationFrom() != null || this.filter.getRegistrationTo() != null)) {
            System.out.println("There is a modifying filter present");
        }
        return new ArrayList<R>(this.registrations);
    }

    public static String getTableName(Class<? extends Entity> cls) {
        return cls.getAnnotation(Table.class).name();
    }

    public void updateIdentification(Session session) {
        //this.identification = (Identification) session.merge(this.identification);
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    /**
     * Set a modifying Query to filter the output, such as limiting which registrations will be included in serialization
     * @param filter
     */
    public void setFilter(Query filter) {
        this.filter = filter;
    }

}
