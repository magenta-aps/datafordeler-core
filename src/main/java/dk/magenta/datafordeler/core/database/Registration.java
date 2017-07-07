package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.*;
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
        this.virkninger = new ArrayList<V>();
    }

    public Registration(OffsetDateTime registreringFra, OffsetDateTime registreringTil, int sekvensnummer) {
        this();
        this.registreringFra = registreringFra;
        this.registreringTil = registreringTil;
        this.sekvensnummer = sekvensnummer;
    }

    public Registration(LocalDate registreringFra, LocalDate registreringTil, int sekvensnummer) {
        this(
                registreringFra != null ? OffsetDateTime.of(registreringFra, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null,
                registreringTil != null ? OffsetDateTime.of(registreringTil, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null,
            sekvensnummer
        );
    }

    public Registration(TemporalAccessor registreringFra, TemporalAccessor registreringTil, int sekvensnummer) {
        this(
                registreringFra != null ? OffsetDateTime.from(registreringFra) : null,
                registreringTil != null ? OffsetDateTime.from(registreringTil) : null,
            sekvensnummer
        );
    }

    /**
     * @param registreringFra A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * @param registreringTil A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Registration(String registreringFra, String registreringTil, int sekvensnummer) {
        this(
                registreringFra != null ? OffsetDateTime.parse(registreringFra) : null,
                registreringTil != null ? OffsetDateTime.parse(registreringTil) : null,
            sekvensnummer
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
    @Filter(name = Effect.FILTER_EFFECT_FROM, condition="(virkningTil >= :"+Effect.FILTERPARAM_EFFECT_FROM+" OR virkningTil is null)")
    @Filter(name = Effect.FILTER_EFFECT_TO, condition="(virkningFra < :"+Effect.FILTERPARAM_EFFECT_TO+")")
    protected List<V> virkninger;

    @JsonProperty(value = "virkninger")
    @XmlElement(name = "virkninger")
    @JacksonXmlProperty(localName = "virkninger")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<V> getVirkninger() {
        return this.virkninger;
    }


    public V getEffect(OffsetDateTime virkningFra, OffsetDateTime virkningTil) {
        for (V effect : this.virkninger) {
            if ((effect.getVirkningFra() == null ? virkningFra == null : effect.getVirkningFra().equals(virkningFra)) &&
                (effect.getVirkningTil() == null ? virkningTil == null : effect.getVirkningTil().equals(virkningTil))) {
                return effect;
            }
        }
        return null;
    }

    public V getEffect(LocalDateTime virkningFra, LocalDateTime virkningTil) {
        return this.getEffect(
                virkningFra != null ? OffsetDateTime.of(virkningFra, ZoneOffset.UTC) : null,
                virkningTil != null ? OffsetDateTime.of(virkningTil, ZoneOffset.UTC) : null
        );
    }

    public V getEffect(LocalDate virkningFra, LocalDate virkningTil) {
        return this.getEffect(
                virkningFra != null ? OffsetDateTime.of(virkningFra, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null,
                virkningTil != null ? OffsetDateTime.of(virkningTil, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null
        );
    }

    public void addEffect(V effect) {
        if (!this.virkninger.contains(effect)) {
            this.virkninger.add(effect);
        }
    }

    public void removeEffect(V effect) {
        // Be sure to also delete the effect yourself, since it still points to the Registration
        this.virkninger.remove(effect);
    }

    @JsonProperty
    public void setVirkninger(Collection<V> virkninger) {
        this.virkninger = new ArrayList<V>(virkninger);
    }




    @Column(nullable = false, insertable = true, updatable = false)
    protected OffsetDateTime registreringFra;

    @JsonProperty(value = "registreringFra")
    @XmlElement
    @XmlJavaTypeAdapter(type = OffsetDateTime.class, value = OffsetDateTimeAdapter.class)
    public OffsetDateTime getRegistreringFra() {
        return this.registreringFra;
    }

    @JsonProperty(value = "registreringFra")
    public void setRegistreringFra(OffsetDateTime registreringFra) {
        this.registreringFra = registreringFra;
    }




    @Column(nullable = true, insertable = true, updatable = false)
    protected OffsetDateTime registreringTil;

    @JsonProperty(value = "registreringTil")
    @XmlElement
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value= OffsetDateTimeAdapter.class)
    public OffsetDateTime getRegistreringTil() {
        return this.registreringTil;
    }

    @JsonProperty(value = "registreringTil")
    public void setRegistreringTil(OffsetDateTime registreringTil) {
        this.registreringTil = registreringTil;
    }



    @Column(nullable = false, insertable = true, updatable = false)
    protected int sekvensnummer;

    @JsonProperty(value = "sekvensnummer")
    @XmlElement
    public int getSekvensnummer() {
        return this.sekvensnummer;
    }

    @JsonProperty(value = "sekvensnummer")
    public void setSekvensnummer(int sekvensnummer) {
        this.sekvensnummer = sekvensnummer;
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
            Identification identification = this.entity.getIdentifikation();
            s.add(subIndentString + "entity: " + identification.getUuid()+" @ "+identification.getDomaene());
        } else {
            s.add(subIndentString + "entity: NULL");
        }
        s.add(subIndentString + "checksum: "+this.registerChecksum);
        s.add(subIndentString + "from: "+this.registreringFra);
        s.add(subIndentString + "to: "+this.registreringTil);
        s.add(subIndentString + "virkninger: [");
        for (V effect : this.virkninger) {
            s.add(effect.toString(indent + 2));
        }
        s.add(subIndentString + "]");
        s.add(indentString+"}");
        return s.toString();
    }

    public void forceLoad(Session session) {
        for (V effect : this.virkninger) {
            effect.forceLoad(session);
        }
    }

}
