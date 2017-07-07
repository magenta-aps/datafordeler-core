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
    protected Identification identifikation;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entity")
    @Filter(name = Registration.FILTER_REGISTRATION_FROM, condition="registreringTil >= :"+Registration.FILTERPARAM_REGISTRATION_FROM+" OR registreringTil is null")
    @Filter(name = Registration.FILTER_REGISTRATION_TO, condition="registreringFra < :"+Registration.FILTERPARAM_REGISTRATION_TO)
    @OrderBy("sekvensnummer") // Refers to sekvensnummer in Registration class
    protected List<R> registreringer;

    @Transient
    @JsonIgnore
    private Query filter = null;

    public Entity() {
        this.registreringer = new ArrayList<R>();
        this.identifikation = new Identification();
    }

    public Entity(Identification identifikation) {
        this();
        this.identifikation = identifikation;
    }

    public Entity(UUID uuid, String domain) {
        this(new Identification(uuid, domain));
    }

    @JsonIgnore
    public Identification getIdentifikation() {
        return this.identifikation;
    }

    @JsonProperty("uuid")
    public UUID getUUID() {
        return this.identifikation.getUuid();
    }

    public void setIdentifikation(Identification identification) {
        this.identifikation = identification;
    }

    @JsonProperty("uuid")
    public void setUUID(UUID uuid) {
        this.identifikation.setUuid(uuid);
    }

    @JsonProperty("domaene")
    public String getDomain() {
        return this.identifikation.getDomaene();
    }


    @JsonProperty("domaene")
    public void setDomain(String domain) {
        this.identifikation.setDomaene(domain);
    }

    @OrderBy("registrationFra")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @XmlElement(name="registreringer")
    @JacksonXmlProperty(localName = "registreringer")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<R> getRegistreringer() {
        return this.registreringer;
    }

    public void addRegistration(R registration) {
        if (!this.registreringer.contains(registration)) {
            this.registreringer.add(registration);
        }
    }

    public R getRegistration(OffsetDateTime registrationFrom) {
        for (R registration : this.registreringer) {
            if (registration.getRegistreringFra() == null ? registrationFrom == null : registration.getRegistreringFra().equals(registrationFrom)) {
                return registration;
            }
        }
        return null;
    }

    public R getRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        for (R registration : this.registreringer) {
            if ((registration.getRegistreringFra() == null ? registrationFrom == null : registration.getRegistreringFra().equals(registrationFrom)) &&
                    (registration.getRegistreringTil() == null ? registrationTo == null : registration.getRegistreringTil().equals(registrationTo))) {
                return registration;
            }
        }
        return null;
    }

    public void forceLoad(Session session) {
        for (R registration : this.registreringer) {
            registration.forceLoad(session);
        }
    }

}
