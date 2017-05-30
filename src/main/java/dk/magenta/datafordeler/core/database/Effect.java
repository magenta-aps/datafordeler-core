package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
@FilterDef(name=Effect.FILTER_EFFECT_FROM, parameters=@ParamDef(name=Effect.FILTERPARAM_EFFECT_FROM, type="java.time.OffsetDateTime"))
@FilterDef(name=Effect.FILTER_EFFECT_TO, parameters=@ParamDef(name=Effect.FILTERPARAM_EFFECT_TO, type="java.time.OffsetDateTime"))
public abstract class Effect<R extends Registration, V extends Effect, D extends DataItem> extends DatabaseEntry {

    public static final String FILTER_EFFECT_FROM = "effectFromFilter";
    public static final String FILTERPARAM_EFFECT_FROM = "effectFromDate";
    public static final String FILTER_EFFECT_TO = "effectToFilter";
    public static final String FILTERPARAM_EFFECT_TO = "effectToDate";

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonIgnore
    protected R registration;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    protected Set<D> dataItems;

    @Column(nullable = false, insertable = true, updatable = false)
    private OffsetDateTime effectFrom;

    @Column(nullable = true, insertable = true, updatable = false)
    private OffsetDateTime effectTo;

    public Effect() {
        this.dataItems = new HashSet<D>();
    }

    public Effect(R registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        this();
        this.registration = registration;
        registration.effects.add(this);
        this.effectFrom = effectFrom;
        this.effectTo = effectTo;
    }

    public Effect(R registration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        this(
                registration,
                effectFrom != null ? OffsetDateTime.from(effectFrom) : null,
                effectTo != null ? OffsetDateTime.from(effectTo) : null
        );
    }

    /**
     * @param effectFrom A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * @param effectTo A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Effect(R registration, String effectFrom, String effectTo) {
        this(
                registration,
                effectFrom != null ? OffsetDateTime.parse(effectFrom) : null,
                effectTo != null ? OffsetDateTime.parse(effectTo) : null
        );
    }

    public R getRegistration() {
        return this.registration;
    }

    @JsonProperty
    @XmlElement
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value= OffsetDateTimeAdapter.class)
    public OffsetDateTime getEffectFrom() {
        return this.effectFrom;
    }


    @JsonProperty
    @XmlElement
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value= OffsetDateTimeAdapter.class)
    public OffsetDateTime getEffectTo() {
        return this.effectTo;
    }


    @JsonProperty(value = "dataItems", access = JsonProperty.Access.WRITE_ONLY)
    @JacksonXmlProperty(localName = "dataItem")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<D> getDataItems() {
        return new ArrayList(this.dataItems);
    }

    public boolean equalData(V other) {
        return (
                (this.effectFrom == null ? other.getEffectFrom() == null : this.effectFrom.equals(other.getEffectFrom())) &&
                (this.effectTo == null ? other.getEffectTo() == null : this.effectTo.equals(other.getEffectTo()))
        );
    }

    public static String getTableName(Class<? extends Effect> cls) {
        return cls.getAnnotation(Table.class).name();
    }

    @XmlElementWrapper(name="data")
    public Map<String, Object> getData() {
        HashMap<String, Object> data = new HashMap<>();
        for (DataItem d : this.dataItems) {
            data.putAll(d.asMap());
        }
        return data;
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
        s.add(subIndentString + "from: "+this.effectFrom);
        s.add(subIndentString + "to: "+this.effectTo);
        s.add(subIndentString + "data: [");
        for (D dataItem : this.getDataItems()) {
            s.add(dataItem.toString(indent + 2));
        }
        s.add(subIndentString + "]");
        s.add(indentString + "}");
        return s.toString();
    }

}
