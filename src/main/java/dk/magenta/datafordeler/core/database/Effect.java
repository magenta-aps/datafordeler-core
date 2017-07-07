package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import org.hibernate.Session;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@FilterDef(name=Effect.FILTER_EFFECT_FROM, parameters=@ParamDef(name=Effect.FILTERPARAM_EFFECT_FROM, type="java.time.OffsetDateTime"))
@FilterDef(name=Effect.FILTER_EFFECT_TO, parameters=@ParamDef(name=Effect.FILTERPARAM_EFFECT_TO, type="java.time.OffsetDateTime"))
public abstract class Effect<R extends Registration, V extends Effect, D extends DataItem> extends DatabaseEntry {

    public static final String FILTER_EFFECT_FROM = "effectFromFilter";
    public static final String FILTERPARAM_EFFECT_FROM = "effectFromDate";
    public static final String FILTER_EFFECT_TO = "effectToFilter";
    public static final String FILTERPARAM_EFFECT_TO = "effectToDate";

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonIgnore
    protected R registrering;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    protected Set<D> dataItems;

    @Column(nullable = true, insertable = true, updatable = false)
    private OffsetDateTime virkningFra;

    @Column(nullable = true, insertable = true, updatable = false)
    private OffsetDateTime virkningTil;

    public Effect() {
        this.dataItems = new HashSet<D>();
    }

    public Effect(R registrering, OffsetDateTime virkningFra, OffsetDateTime virkningTil) {
        this();
        this.setRegistrering(registrering);
        this.virkningFra = virkningFra;
        this.virkningTil = virkningTil;
    }

    public Effect(R registrering, TemporalAccessor virkningFra, TemporalAccessor virkningTil) {
        this(
            registrering,
                virkningFra != null ? OffsetDateTime.from(virkningFra) : null,
                virkningTil != null ? OffsetDateTime.from(virkningTil) : null
        );
    }

    public Effect(R registrering, LocalDate virkningFra, LocalDate virkningTil) {
        this(
            registrering,
                virkningFra != null ? OffsetDateTime.of(virkningFra, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null,
                virkningTil != null ? OffsetDateTime.of(virkningTil, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null
        );
    }

    /**
     * @param virkningFra A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * @param virkningTil A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Effect(R registrering, String virkningFra, String virkningTil) {
        this(
            registrering,
                virkningFra != null ? OffsetDateTime.parse(virkningFra) : null,
                virkningTil != null ? OffsetDateTime.parse(virkningTil) : null
        );
    }

    public R getRegistrering() {
        return this.registrering;
    }

    protected void setRegistrering(R registrering) {
        if (registrering != null) {
            this.registrering = registrering;
            registrering.addEffect(this);
        }
    }

    @JsonProperty(value = "virkningFra")
    @XmlElement(name = "virkningFra")
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value= OffsetDateTimeAdapter.class)
    public OffsetDateTime getVirkningFra() {
        return this.virkningFra;
    }


    @JsonProperty(value = "virkningTil")
    @XmlElement(name = "virkningTil")
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value= OffsetDateTimeAdapter.class)
    public OffsetDateTime getVirkningTil() {
        return this.virkningTil;
    }

    @JsonIgnore
    public List<D> getDataItems() {
        return new ArrayList(this.dataItems);
    }

    @JsonProperty(value = "data")
    @XmlElementWrapper(name = "data")
    public Map<String, Object> getData() {
        HashMap<String, Object> data = new HashMap<>();
        for (DataItem d : this.dataItems) {
            data.putAll(d.asMap());
        }
        return data;
    }

    @JsonProperty(value = "data"/*, access = JsonProperty.Access.READ_ONLY*/)
    @JacksonXmlProperty(localName = "data")
    @JacksonXmlElementWrapper(useWrapping = false)
    public void setDataItems(Collection<D> items) {
        this.dataItems.addAll(items);
    }

    public boolean equalData(V other) {
        return (
                (this.virkningFra == null ? other.getVirkningFra() == null : this.virkningFra
                    .equals(other.getVirkningFra())) &&
                (this.virkningTil == null ? other.getVirkningTil() == null : this.virkningTil
                    .equals(other.getVirkningTil()))
        );
    }

    public static String getTableName(Class<? extends Effect> cls) {
        return cls.getAnnotation(Table.class).name();
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
        s.add(indentString + this.getClass().getSimpleName() + "["+this.hashCode()+"] {");
        s.add(subIndentString + "from: "+this.virkningFra);
        s.add(subIndentString + "to: "+this.virkningTil);
        s.add(subIndentString + "data: [");
        for (D dataItem : this.getDataItems()) {
            s.add(dataItem.toString(indent + 2));
        }
        s.add(subIndentString + "]");
        s.add(indentString + "}");
        return s.toString();
    }

    public void forceLoad(Session session) {
        for (D dataItem : this.dataItems) {
            dataItem.forceLoad(session);
        }
    }

}
