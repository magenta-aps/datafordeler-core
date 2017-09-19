package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.util.Equality;
import org.hibernate.Session;
import org.hibernate.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 20-02-17.
 * An Entity represents a top-level item in the database as well as in service 
 * output, such as a Person or a Company (to be implemented as subclasses in plugins)
 * Entities usually hold very little data on their own, but links to a series of 
 * bitemporality objects (Registrations, and further down Effects), that in turn 
 * hold leaf nodes (DataItems) containing the bulk of the associated data.
 */
@MappedSuperclass
@Embeddable
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Entity<E extends Entity, R extends Registration> extends DatabaseEntry {

    @Transient
    private Logger log;

    protected Logger getLog() {
        return this.log;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @XmlTransient
    protected Identification identification;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entity")
    @OrderBy("sequenceNumber") // Refers to sequenceNumber in Registration class
    @Filters({
            @Filter(name = Registration.FILTER_REGISTRATION_FROM, condition="(registrationTo >= :"+Registration.FILTERPARAM_REGISTRATION_FROM+" OR registrationTo is null)"),
            @Filter(name = Registration.FILTER_REGISTRATION_TO, condition="(registrationFrom < :"+Registration.FILTERPARAM_REGISTRATION_TO+")")
    })
    protected List<R> registrations;

    @Transient
    @JsonIgnore
    private Query filter = null;

    public Entity() {
        this.registrations = new ArrayList<R>();
        this.identification = new Identification();
        this.log = LoggerFactory.getLogger(this.getClass());
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

    @JsonProperty("UUID")
    public UUID getUUID() {
        return this.identification.getUuid();
    }

    public void setIdentifikation(Identification identification) {
        this.identification = identification;
    }

    @JsonProperty("uuid")
    public void setUUID(UUID uuid) {
        this.identification.setUuid(uuid);
    }

    @JsonProperty("domaene")
    public String getDomain() {
        return this.identification.getDomain();
    }


    @JsonProperty("domaene")
    public void setDomain(String domain) {
        this.identification.setDomain(domain);
    }

    @OrderBy("registrationFrom")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "registreringer")
    @XmlElement(name="registreringer")
    @JacksonXmlProperty(localName = "registreringer")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<R> getRegistrations() {
        return this.registrations;
    }

    /**
     * Add a registration to the list of registrations in this entity
     */
    public void addRegistration(R registration) {
        if (!this.registrations.contains(registration)) {
            this.registrations.add(registration);
        }
    }

    /**
     * Finds a registration under this entity that starts at the given OffsetDateTime
     * Returns null if none are found
     */
    public R getRegistration(OffsetDateTime registrationFrom) {
        for (R registration : this.registrations) {
            if (registration.getRegistrationFrom() == null ? registrationFrom == null : registration.getRegistrationFrom().equals(registrationFrom)) {
                return registration;
            }
        }
        return null;
    }

    /**
     * Finds a registration under this entity that starts and ends at the given
     * OffsetDateTime pair
     * Returns null if none are found
     */
    public R getRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        for (R registration : this.registrations) {
            if ((registration.getRegistrationFrom() == null ? registrationFrom == null : registration.getRegistrationFrom().equals(registrationFrom)) &&
                    (registration.getRegistrationTo() == null ? registrationTo == null : registration.getRegistrationTo().equals(registrationTo))) {
                return registration;
            }
        }
        return null;
    }

    public List<R> getRegistrationsStartingBetween(OffsetDateTime start, OffsetDateTime end) {
        ArrayList<R> registrations = new ArrayList<R>();
        for (R registration : this.getRegistrations()) {
            OffsetDateTime from = registration.getRegistrationFrom();
            if (from != null && (from.isEqual(start) || from.isAfter(start)) && (end == null || from.isBefore(end))) {
                registrations.add(registration);
            }
        }
        return registrations;
    }

    public R getRegistrationAt(OffsetDateTime dateTime) {
        for (R registration : this.getRegistrations()) {
            OffsetDateTime from = registration.getRegistrationFrom();
            OffsetDateTime to = registration.getRegistrationTo();

            if (!from.isAfter(dateTime) &&
                (to == null || to.isAfter(dateTime))) {
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

    /**
     * For import purposes, creates an empty Registration of the associated class, 
     * pointing to this Entity
     */
    public final R createRegistration() {
        R registration = this.createEmptyRegistration();
        registration.setEntity(this);
        return registration;
    }





    public List<R> findRegistrations(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        // Find/create all necessary Registrations
        Logger log = this.getLog();
        log.debug("Finding registrations within bounds "+registrationFrom+" - "+registrationTo);

        ArrayList<R> registrations = new ArrayList<>();
        ArrayList<R> orderedRegistrations = new ArrayList<>(this.getRegistrations());
        Collections.sort(orderedRegistrations);

        OffsetDateTime latestEnd = OffsetDateTime.MIN;
        for (R existingRegistration : orderedRegistrations) {
            R registration = existingRegistration;
            log.debug("Looking at registration "+registration.getRegistrationFrom()+" - "+registration.getRegistrationTo());

            // There is a gap, or a missing registration at the start
            if (
                    latestEnd != null &&
                    latestEnd.isBefore(nFrom(registration.getRegistrationFrom())) &&
                            (latestEnd.isAfter(nFrom(registrationFrom)) || nFrom(registrationFrom).isBefore(nFrom(registration.getRegistrationFrom())))
                    ) {
                log.debug("Gap found at "+latestEnd+" - "+registration.getRegistrationFrom()+", creating registration");
                R newReg = this.createRegistration();
                newReg.setRegistrationFrom(latestEnd.isEqual(OffsetDateTime.MIN) ? registrationFrom : latestEnd);
                newReg.setRegistrationTo(registration.getRegistrationFrom());
                registrations.add(newReg);
            }
            latestEnd = registration.getRegistrationTo();

            // If the registration starts before our requested start, but ends after, we must do a split and move on with the second part
            if (
                    nFrom(registration.getRegistrationFrom()).isBefore(nFrom(registrationFrom)) &&
                            nTo(registration.getRegistrationTo()).isAfter(nFrom(registrationFrom))
                    ) {
                log.debug("Registration straddles our start, split it at "+registrationFrom);
                registration = (R) registration.split(registrationFrom);
                log.debug("Registration is now "+registration.getRegistrationFrom()+" - "+registration.getRegistrationTo());
            }
            // If the registration ends after our requested end, but begins before that, do a split
            if (
                    nFrom(registration.getRegistrationFrom()).isBefore(nTo(registrationTo)) &&
                            nTo(registration.getRegistrationTo()).isAfter(nTo(registrationTo))
                    ) {
                log.debug("Registration straddles our end, split it at "+registrationTo);
                registration.split(registrationTo);
                log.debug("Registration is now "+registration.getRegistrationFrom()+" - "+registration.getRegistrationTo());
            }
            // If the registration lies within our bounds, include it
            if (
                    (
                            Equality.equal(registration.getRegistrationFrom(), registrationFrom) || // Check exact match
                                    (nFrom(registration.getRegistrationFrom()).isAfter(nFrom(registrationFrom))) // Check if it lies after our requested start
                    ) && (
                            Equality.equal(registration.getRegistrationTo(), registrationTo) || // Check exact match
                                    (nTo(registration.getRegistrationTo()).isBefore(nTo(registrationTo))) // Check if it lies before our requested end
                    )
                    ) {
                log.debug("Registration lies within bounds, adding it to list");
                registrations.add(registration);
            }
        }

        // If the last existing registration ends before our requested end, create a new registration there
        OffsetDateTime requestedEndTime = registrationTo == null ? OffsetDateTime.MAX : registrationTo;
        if (latestEnd != null && latestEnd.isBefore(requestedEndTime)) {
            log.debug("Last registrations ended before our requested end, create missing registration at "+(latestEnd.isEqual(OffsetDateTime.MIN) ? registrationFrom : latestEnd)+" - "+registrationTo);
            R registration = this.createRegistration();
            registration.setRegistrationFrom(latestEnd.isEqual(OffsetDateTime.MIN) ? registrationFrom : latestEnd);
            registration.setRegistrationTo(registrationTo);
            registrations.add(registration);
        }

        orderedRegistrations = new ArrayList<>(this.getRegistrations());
        Collections.sort(orderedRegistrations);
        int seqNo = 0;
        for (R registration : orderedRegistrations) {
            registration.setSequenceNumber(seqNo);
            seqNo++;
        }

        return registrations;
    }

    private static OffsetDateTime nFrom(OffsetDateTime a) {
        if (a == null) return OffsetDateTime.MIN;
        return a;
    }

    private static OffsetDateTime nTo(OffsetDateTime a) {
        if (a == null) return OffsetDateTime.MAX;
        return a;
    }

}
