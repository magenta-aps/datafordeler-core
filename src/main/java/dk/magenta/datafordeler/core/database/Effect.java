package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

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

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonProperty
    protected Set<D> dataItems;

    @Column(nullable = false, insertable = true, updatable = false)
    @JsonProperty
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
    private OffsetDateTime effectFrom;

    @Column(nullable = true, insertable = true, updatable = false)
    @JsonProperty
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
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

    public OffsetDateTime getEffectFrom() {
        return this.effectFrom;
    }

    public OffsetDateTime getEffectTo() {
        return this.effectTo;
    }

    public Set<D> getDataItems() {
        return this.dataItems;
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

    public String toString() {
        return this.toString(0);
    }

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
