package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@FilterDef(name=Registration.FILTER_REGISTRATION_FROM, parameters=@ParamDef(name=Registration.FILTERPARAM_REGISTRATION_FROM, type="java.time.OffsetDateTime"))
@FilterDef(name=Registration.FILTER_REGISTRATION_TO, parameters=@ParamDef(name=Registration.FILTERPARAM_REGISTRATION_TO, type="java.time.OffsetDateTime"))
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public abstract class Registration<E extends Entity, R extends Registration, V extends Effect> extends DatabaseEntry {

    public static final String FILTER_REGISTRATION_FROM = "registrationFromFilter";
    public static final String FILTERPARAM_REGISTRATION_FROM = "registrationFromDate";
    public static final String FILTER_REGISTRATION_TO = "registrationToFilter";
    public static final String FILTERPARAM_REGISTRATION_TO = "registrationToDate";

    public Registration() {
        this.effects = new ArrayList<V>();
    }

    public Registration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, int sequenceNumber) {
        this();
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
        this.sequenceNumber = sequenceNumber;
    }

    public Registration(TemporalAccessor registrationFrom, TemporalAccessor registrationTo, int sequenceNumber) {
        this(
                registrationFrom != null ? OffsetDateTime.from(registrationFrom) : null,
                registrationTo != null ? OffsetDateTime.from(registrationTo) : null,
                sequenceNumber
        );
    }

    /**
     * @param registrationFrom A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * @param registrationTo A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Registration(String registrationFrom, String registrationTo, int sequenceNumber) {
        this(
                registrationFrom != null ? OffsetDateTime.parse(registrationFrom) : null,
                registrationTo != null ? OffsetDateTime.parse(registrationTo) : null,
                sequenceNumber
        );
    }




    @ManyToOne(cascade = CascadeType.ALL)
    protected E entity;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @XmlTransient
    public E getEntity() {
        return this.entity;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setEntity(E entity) {
        this.entity = entity;
        this.entity.addRegistration(this);
    }





    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Filter(name = Effect.FILTER_EFFECT_FROM, condition="(effectTo >= :"+Effect.FILTERPARAM_EFFECT_FROM+" OR effectTo is null)")
    @Filter(name = Effect.FILTER_EFFECT_TO, condition="(effectFrom < :"+Effect.FILTERPARAM_EFFECT_TO+")")
    protected List<V> effects;

    @JsonProperty(value = "effects")
    @XmlElement(name = "effect")
    @JacksonXmlProperty(localName = "effect")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<V> getEffects() {
        return this.effects;
    }


    public V getEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        for (V effect : this.effects) {
            if ((effect.getEffectFrom() == null ? effectFrom == null : effect.getEffectFrom().equals(effectFrom)) &&
                (effect.getEffectTo() == null ? effectTo == null : effect.getEffectTo().equals(effectTo))) {
                return effect;
            }
        }
        return null;
    }

    public void removeEffect(V effect) {
        // Be sure to also delete the effect yourself, since it still points to the Registration
        this.effects.remove(effect);
    }

    @JsonProperty
    public void setEffects(Collection<V> effects) {
        this.effects = new ArrayList<V>(effects);
    }




    @Column(nullable = false, insertable = true, updatable = false)
    protected OffsetDateTime registrationFrom;

    @JsonProperty(value = "registrationFrom")
    @XmlElement
    @XmlJavaTypeAdapter(type = OffsetDateTime.class, value = OffsetDateTimeAdapter.class)
    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFrom;
    }

    @JsonProperty(value = "registrationFrom")
    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = registrationFrom;
    }




    @Column(nullable = true, insertable = true, updatable = false)
    protected OffsetDateTime registrationTo;

    @JsonProperty(value = "registrationTo")
    @XmlElement
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value= OffsetDateTimeAdapter.class)
    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    @JsonProperty(value = "registrationTo")
    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = registrationTo;
    }



    @Column(nullable = false, insertable = true, updatable = false)
    protected int sequenceNumber;

    @JsonProperty(value = "sequenceNumber")
    @XmlElement
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    @JsonProperty(value = "sequenceNumber")
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }


    // The checksum as reported by the register
    protected String registerChecksum;

    @JsonProperty("checksum")
    public String getRegisterChecksum() {
        return this.registerChecksum;
    }


    @JsonProperty("checksum")
    public void setRegisterChecksum(String registerChecksum) {
        this.registerChecksum = registerChecksum;
    }


    /**
     * Pretty-print contained data
     * @return Compiled string output
     */
    public String toString() {
        return this.toString(0);
    }

    /**
     * Pretty-print contained data
     * @param indent Number of spaces to indent the output with
     * @return Compiled string output
     */
    public String toString(int indent) {
        String indentString = new String(new char[4 * (indent)]).replace("\0", " ");
        String subIndentString = new String(new char[4 * (indent + 1)]).replace("\0", " ");
        StringJoiner s = new StringJoiner("\n");
        s.add(indentString + this.getClass().getSimpleName()+"["+this.hashCode()+"] {");
        if (this.entity != null) {
            Identification identification = this.entity.getIdentification();
            s.add(subIndentString + "entity: " + identification.getUuid()+" @ "+identification.getDomain());
        } else {
            s.add(subIndentString + "entity: NULL");
        }
        s.add(subIndentString + "checksum: "+this.registerChecksum);
        s.add(subIndentString + "from: "+this.registrationFrom);
        s.add(subIndentString + "to: "+this.registrationTo);
        s.add(subIndentString + "effects: [");
        for (V effect : this.effects) {
            s.add(effect.toString(indent + 2));
        }
        s.add(subIndentString + "]");
        s.add(indentString+"}");
        return s.toString();
    }

}
