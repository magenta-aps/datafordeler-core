package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dk.magenta.datafordeler.core.fapi.Query;
import org.hibernate.Session;
import org.hibernate.annotations.*;
import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
@Embeddable
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Entity<E extends Entity, R extends Registration> extends DatabaseEntry {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @XmlTransient
    protected Identification identification;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entity")
    @Filter(name = Registration.FILTER_REGISTRATION_FROM, condition="registrationTo >= :"+Registration.FILTERPARAM_REGISTRATION_FROM+" OR registrationTo is null")
    @Filter(name = Registration.FILTER_REGISTRATION_TO, condition="registrationFrom < :"+Registration.FILTERPARAM_REGISTRATION_TO)
    @OrderBy("sequenceNumber")
    protected List<R> registrations;

    @Transient
    @JsonIgnore
    private Query filter = null;

    public Entity() {
        this.registrations = new ArrayList<R>();
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
    public Identification getIdentification() {
        return this.identification;
    }

    @JsonProperty("uuid")
    public UUID getUUID() {
        return this.identification.getUuid();
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
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

    @OrderBy("registrationFrom")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @XmlElement(name="registration")
    @JacksonXmlProperty(localName = "registration")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<R> getRegistrations() {
        return this.registrations;
    }

    public void addRegistration(R registration) {
        if (!this.registrations.contains(registration)) {
            this.registrations.add(registration);
        }
    }

    public R getRegistration(OffsetDateTime registrationFrom) {
        for (R registration : this.registrations) {
            if (registration.getRegistrationFrom() == null ? registrationFrom == null : registration.getRegistrationFrom().equals(registrationFrom)) {
                return registration;
            }
        }
        return null;
    }

    public R getRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        for (R registration : this.registrations) {
            if ((registration.getRegistrationFrom() == null ? registrationFrom == null : registration.getRegistrationFrom().equals(registrationFrom)) &&
                    (registration.getRegistrationTo() == null ? registrationTo == null : registration.getRegistrationTo().equals(registrationTo))) {
                return registration;
            }
        }
        return null;
    }

    public void forceLoad(Session session) {
        for (R registration : this.registrations) {
            registration.forceLoad(session);
        }
    }

    protected abstract R createEmptyRegistration();

    public final R createRegistration() {
        R registration = this.createEmptyRegistration();
        registration.setEntity(this);
        return registration;
    }

}
