package dk.magenta.datafordeler.core.model;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Effect<E extends Entity, R extends Registration, V extends Effect, D extends DataItem> {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    protected R registration;

    @ManyToMany(cascade = CascadeType.ALL)
    protected Set<D> dataItems;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, insertable = true, updatable = false)
    protected OffsetDateTime effectFrom;

    @Column(nullable = true, insertable = true, updatable = false)
    protected OffsetDateTime effectTo;

    public Effect() {}

    private Effect(R registration) {
        this.registration = registration;
        this.dataItems = new HashSet<D>();
        this.registration.effects.add(this);
    }

    public Effect(R registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        this(registration);
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
     * @param registration
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

    public OffsetDateTime getEffectFrom() {
        return this.effectFrom;
    }

    public OffsetDateTime getEffectTo() {
        return this.effectTo;
    }

    public Set<D> getDataItems() {
        return this.dataItems;
    }

    public static String getTableName(Class<? extends Effect> cls) {
        return cls.getAnnotation(Table.class).name();
    }

}
